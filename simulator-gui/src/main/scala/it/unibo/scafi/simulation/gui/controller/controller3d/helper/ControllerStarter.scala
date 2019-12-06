/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.gui.controller.controller3d.helper

import it.unibo.scafi.simulation.gui.controller.ControllerUtils
import it.unibo.scafi.simulation.gui.model.SimulationManager
import it.unibo.scafi.simulation.gui.model.implementation.NetworkImpl
import it.unibo.scafi.simulation.gui.view.ui3d.SimulatorUI3D
import it.unibo.scafi.simulation.gui.{Settings, Simulation}

private[controller3d] object ControllerStarter {

  def startup(simulation: Simulation, gui: SimulatorUI3D, simulationManager: SimulationManager): Unit = {
    simulation.setSelectionAttemptedDependency(() => gui.getSimulationPanel.isAttemptingSelection)
    ControllerUtils.setupSensors(Settings.Sim_Sensors)
    ControllerUtils.enableMenuBar(enable = true, gui.getJMenuBar)
    startSimulation(simulation, gui, simulationManager)
  }

  def startSimulation(simulation: Simulation, gui: SimulatorUI3D, simulationManager: SimulationManager): Unit = {
    val nodes = NodesGenerator.createNodes(Settings.Sim_Topology, Settings.Sim_NumNodes, Settings.ConfigurationSeed)
    nodes.values.foreach(node => gui.getSimulationPanel.addNode(node.position, node.id.toString))
    val policyNeighborhood = ControllerUtils.getNeighborhoodPolicy
    simulation.network = new NetworkImpl(nodes, policyNeighborhood)
    simulation.setDeltaRound(Settings.Sim_DeltaRound)
    simulation.setRunProgram(Settings.Sim_ProgramClass)
    simulation.setStrategy(Settings.Sim_ExecStrategy)
    simulationManager.simulation = simulation
    simulationManager.setPauseFire(Settings.Sim_DeltaRound)
    simulationManager.start()
  }

  def setupGUI(gui: SimulatorUI3D): Unit = {
    val gui3d = gui.getSimulationPanel
    gui3d.setConnectionsVisible(Settings.Sim_DrawConnections)
    gui3d.setSelectionColor(Settings.Color_selection)
    gui3d.setNodesColor(Settings.Color_device)
    gui3d.setConnectionsColor(Settings.Color_link)
    gui3d.setBackground(Settings.Color_background)
  }

}
