/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.model.application;

import ch.fhnw.lernstickwelcome.model.Processable;
import ch.fhnw.lernstickwelcome.model.WelcomeModelFactory;
import ch.fhnw.lernstickwelcome.model.application.proxy.ProxyTask;
import ch.fhnw.util.ProcessExecutor;
import javafx.concurrent.Task;

/**
 * After Installation of application with {@link ApplicationTask}, this Task is
 * called to fix dependencies using {@code apt-get -f -y --force-yes install}.
 * 
 * @author sschw
 */
public class InstallPostprocessingTask implements Processable<String> {
    private final static ProcessExecutor PROCESS_EXECUTOR = WelcomeModelFactory.getProcessExecutor();
    private final ProxyTask proxy;
    private final ApplicationGroupTask[] groups;
    
    /**
     * Initializes the InstallPostprocessingTask.<br>
     * Needs ApplicationGroupTasks to ensure that there are installations to
     * install.
     *
     * @param proxy The proxy which should be used to run its tasks.
     * @param groups The application groups that will be installed.
     */
    public InstallPostprocessingTask(ProxyTask proxy, ApplicationGroupTask... groups) {
        this.proxy = proxy;
        this.groups = groups;
    }

    @Override
    public Task<String> newTask() {
        return new InternalTask();
    }

    private class InternalTask extends Task<String> {

        @Override
        protected String call() throws Exception {
            // Check if there are applications to install.
            int appsToInstall = 0;
            for(ApplicationGroupTask g : groups) 
                appsToInstall += g.getApps().stream().
                        filter(a -> !a.installedProperty().get() && a.installingProperty().get()).count();
            
            if(appsToInstall > 0) {
                updateTitle("InstallPostprocessingTask.title");
                updateMessage("InstallPostprocessingTask.message");
                updateProgress(0, 1);
                String script = "apt-get" + proxy.getAptGetProxy() + "-f -y --force-yes install";
                PROCESS_EXECUTOR.executeScript(script);
            }
            updateProgress(1, 1);
            return null;
        }

    }

}