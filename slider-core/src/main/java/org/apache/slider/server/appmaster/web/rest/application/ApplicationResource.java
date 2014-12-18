/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.slider.server.appmaster.web.rest.application;

import com.google.common.collect.Lists;
import org.apache.slider.api.types.SerializedContainerInformation;
import org.apache.slider.core.conf.ConfTree;
import org.apache.slider.server.appmaster.state.StateAccessForProviders;
import org.apache.slider.server.appmaster.web.WebAppApi;
import org.apache.slider.server.appmaster.web.rest.AbstractSliderResource;
import org.apache.slider.server.appmaster.web.rest.RestPaths;
import org.apache.slider.server.appmaster.web.rest.application.resources.CachedContent;
import org.apache.slider.server.appmaster.web.rest.application.resources.ContainerListRefresher;
import org.apache.slider.server.appmaster.web.rest.application.resources.ContentCache;
import org.apache.slider.server.appmaster.web.rest.application.resources.LiveResourcesRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Singleton
public class ApplicationResource extends AbstractSliderResource {
  private static final Logger log =
      LoggerFactory.getLogger(ApplicationResource.class);

  public static final int LIFESPAN = 1000;
  private final ContentCache cache = new ContentCache();

  public ApplicationResource(WebAppApi slider) {
    super(slider);
    StateAccessForProviders state = slider.getAppState();
    cache.put(RestPaths.LIVE_RESOURCES,
        new CachedContent<ConfTree>(LIFESPAN,
            new LiveResourcesRefresher(state)));
    cache.put(RestPaths.LIVE_CONTAINERS,
        new CachedContent<Map<String, SerializedContainerInformation>>(LIFESPAN,
            new ContainerListRefresher(state)));
  }

  /**
   * Build a new JSON-marshallable list of string elements
   * @param elements elements
   * @return something that can be returned
   */
  private List<String> toJsonList(String... elements) {
    return Lists.newArrayList(elements);
  }

  @GET
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON})
  public List<String> getRoot() {
    return toJsonList("model", "live", "actions");
  }

  @GET
  @Path("/model")
  @Produces({MediaType.APPLICATION_JSON})
  public List<String> getModel() {
    return toJsonList("desired", "resolved");
  }

  @GET
  @Path("/live")
  @Produces({MediaType.APPLICATION_JSON})
  public List<String> getLive() {
    return toJsonList("resources",
        "containers",
        "components",
        "nodes",
        "statistics",
        "internal");
  }

  @GET
  @Path(RestPaths.LIVE_RESOURCES)
  @Produces({MediaType.APPLICATION_JSON})
  public Object getLiveResources() {
    try {
      return cache.lookup(RestPaths.LIVE_RESOURCES);
    } catch (Exception e) {
      throw buildException(RestPaths.LIVE_RESOURCES, e);
    }
  }
  @GET
  @Path(RestPaths.LIVE_CONTAINERS)
  @Produces({MediaType.APPLICATION_JSON})
  public Map<String, SerializedContainerInformation> getLiveContainers() {
    try {
      return (Map<String, SerializedContainerInformation>)cache.lookup(
          RestPaths.LIVE_CONTAINERS);
    } catch (Exception e) {
      throw buildException(RestPaths.LIVE_CONTAINERS, e);
    }
  }

}