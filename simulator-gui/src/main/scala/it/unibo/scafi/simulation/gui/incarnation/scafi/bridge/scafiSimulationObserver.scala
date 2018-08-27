package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.controller.logger.LogManager.IntLog
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodeDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesAdded
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.sensorInput
import it.unibo.scafi.space.Point3D
object scafiSimulationObserver extends ScafiBridge {
  private val OutputChannel = "simulation round"
  private val checkMoved = world.createObserver(Set(NodesMoved))
  private val checkChanged = world.createObserver(Set(NodeDeviceChanged))
  private val checkAdded = world.createObserver(Set(NodesAdded))
  private var exportProduced : Map[ID,(ScafiLikeWorld,world.ID) => Unit] = Map()
  private var tick = 0L;
  private var time = 0L;
  private var block = false; //used to has a blocking access to list
  private var printTime = 0;
  private var values = List[Long]()
  override protected val maxDelta: Option[Int] = None
  override protected def AsyncLogicExecution(): Unit = {
    if(block) return;
    if(contract.simulation.isDefined) {
      val net = contract.simulation.get
      val before = System.nanoTime()
      val result = net.exec(runningContext)
      time += System.nanoTime() - before
      val action = this.simulationSeed.get.action
      tick += 1;
      if(action.isDefinedAt(result._2)) {
        exportProduced += result._1 -> action(result._2)
      }
      // TODO CHIEDI COME GESTIRE GLI EXPORT exportProduced :::= (actions.filter { x => x.isDefinedAt(result._2) } map { x => result._1 -> x(result._2) }).toList
    }
    if(time > 1000000000L) {
      import it.unibo.scafi.simulation.gui.controller.logger.{LogManager => log}

      values ::= tick
      log.notify(IntLog(OutputChannel, "instant", tick.toInt))
      /*println("current : " + tick)
      println("min : " + values.min)
      println("max : " + values.max)
      println("avg : " + values.sum / values.size)
      println("time alapsed : " + printTime)*/
      tick = 0;
      time = 0;
      printTime += 1
    }
  }
  //TODO PERFORMANCE ISSUE! CHECK OUT!
  override def onTick(float: Float): Unit = {
    val moved = checkMoved.nodeChanged()
    val devs = checkChanged.nodeChanged()
    val added = checkAdded.nodeChanged()
    if(contract.simulation.isDefined) {
      val extern = contract.simulation.get
      devs map {world(_).get} foreach {x => x.devices.filter{y => y.stream == sensorInput} foreach(y => {extern.chgSensorValue(y.name,Set(x.id),y.value)})}
      moved foreach { x =>
        val node = world(x).get
        val oldNeigh = contract.simulation.get.neighbourhood(x)
        contract.simulation.get.setPosition((x), Point3D(node.position.x, node.position.y, node.position.z))
        val neigh = contract.simulation.get.neighbourhood(x)
        world.network.setNeighbours(x,neigh)
        (oldNeigh ++ neigh) foreach {x => {world.network.setNeighbours(x,contract.simulation.get.neighbourhood(x))}}
      }
    }
    block = true
    val toCompute = exportProduced
    exportProduced = Map.empty
    block = false
    val toComputeMap = toCompute
    toComputeMap foreach { x => x._2(world,x._1)}
  }
}


