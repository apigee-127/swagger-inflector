/*
 *  Copyright 2015 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.swagger.inflector.processors;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.inflector.examples.XmlExampleSerializer;
import io.swagger.inflector.examples.models.Example;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/yaml"})
public class ExampleSerializer implements MessageBodyWriter<Example> {
    static boolean prettyPrint = false;
    Logger LOGGER = LoggerFactory.getLogger(ExampleSerializer.class);

    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(new JsonExampleSerializer());
        Json.mapper().registerModule(simpleModule);
    }

    public static void setPrettyPrint(boolean shouldPrettyPrint) {
        ExampleSerializer.prettyPrint = shouldPrettyPrint;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                               MediaType mediaType) {
        return Example.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Example data, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Example data,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> headers,
                        OutputStream out) throws IOException {
        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            if (prettyPrint) {
                out.write(Json.pretty().writeValueAsString(data).getBytes("utf-8"));
            } else {
                out.write(Json.mapper().writeValueAsString(data).getBytes("utf-8"));
            }
        } else if (mediaType.toString().startsWith("application/yaml")) {
            headers.remove("Content-Type");
            headers.add("Content-Type", "application/yaml");
            out.write(Yaml.mapper().writeValueAsString(data).getBytes("utf-8"));
        } else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
            out.write(new XmlExampleSerializer().serialize(data).getBytes("utf-8"));
        }
    }
}

