/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.process.MonitorService;
import org.sonar.process.ProcessWrapper;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {

  private final static Logger LOGGER = LoggerFactory.getLogger(Application.class);


  private static DatagramSocket systemAvailableSocket() throws IOException {
    return new DatagramSocket(0);
  }

  public static void main(String... args) throws InterruptedException, IOException {

    final ExecutorService executor = Executors.newFixedThreadPool(2);
    final MonitorService monitor = new MonitorService(systemAvailableSocket());

    //Create the processes
    //final ProcessWrapper sonarQube = new ProcessWrapper("SQ", monitor);
    final ProcessWrapper elasticsearch = new ProcessWrapper(
      "org.sonar.search.ElasticSearch",
      new String[]{"/Volumes/data/sonar/sonarqube/server/sonar-search/target/sonar-search-4.5-SNAPSHOT.jar"},
      ImmutableMap.of("esPort", "9200", "esHome", "/Volumes/data/sonar/sonarqube/server/sonar-search/target/"),
      "ES", monitor.getMonitoringPort());

    //Register processes to monitor
    monitor.register(elasticsearch);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        LOGGER.info("Shutting down sonar Node");
        //sonarQube.shutdown();
        elasticsearch.interrupt();
        executor.shutdown();
        try {
          executor.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          LOGGER.warn("Executing terminated", e);
        }
      }
    });

    // Start our processes
    LOGGER.info("Starting Child processes...");
    executor.submit(elasticsearch);
    //executor.submit(sonarQube);

    // And monitor the activity
    monitor.run();
    LOGGER.warn("Shutting down the node...");

    // If monitor is finished, we're done. Cleanup
    executor.shutdownNow();
  }
}