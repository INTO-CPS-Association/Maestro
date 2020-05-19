package org.intocps.maestro.webapi.controllers;

import org.intocps.maestro.webapi.maestro2.Maestro2SimulationController;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;

public class SessionLogic {
    public final File rootDirectory;
    private Maestro2SimulationController.InitializationData initializationData;
    private Maestro2SimulationController.SimulateRequestBody simulateRequestBody;
    private WebSocketSession socket;

    public SessionLogic(File rootDirectory) {
        rootDirectory.mkdir();
        this.rootDirectory = rootDirectory;

    }

    public Maestro2SimulationController.InitializationData getInitializationData() {
        return initializationData;
    }

    public void setInitializationData(Maestro2SimulationController.InitializationData initializationData) {
        this.initializationData = initializationData;
    }

    public Maestro2SimulationController.SimulateRequestBody getSimulateRequestBody() {
        return simulateRequestBody;
    }

    public void setSimulateRequestBody(Maestro2SimulationController.SimulateRequestBody simulateRequestBody) {
        this.simulateRequestBody = simulateRequestBody;
    }

    public void setWebsocketSession(WebSocketSession socket) {
        this.socket = socket;
    }

    public Boolean containsSocket() {
        if (this.socket == null) {
            return false;
        } else {
            if (this.socket.isOpen()) {
                return true;
            } else {
                this.socket = null;
                return false;
            }
        }
    }

    public void removeSocket() {
        this.socket = null;
    }
}
