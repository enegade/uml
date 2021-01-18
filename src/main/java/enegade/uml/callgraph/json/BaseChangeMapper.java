package enegade.uml.callgraph.json;

/*
 * Copyright 2020 enegade
 *
 * This file is part of the enegade.uml package.
 *
 * This file is distributed "AS IS", WITHOUT ANY WARRANTIES.
 *
 * For the full copyright and license information, please view the LICENSE
 * file distributed with this project.
 */

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import enegade.uml.callgraph.SelectorChangeMapper;
import enegade.uml.selector.TypeSelector;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author enegade
 */
public abstract class BaseChangeMapper  implements SelectorChangeMapper
{
    private String jsonPath;

    public BaseChangeMapper(String jsonPath)
    {
        this.jsonPath = jsonPath;
    }

    protected boolean shouldSkipField(FieldAttributes f)
    {
        if( f.getDeclaringClass().getName().equals("enegade.uml.selector.TypeSelector") ) {
            for( String methodName: new String[] {"fields", "objects", "selected"} ) {
                if( f.getName().equals(methodName) ) {
                    return true;
                }
            }
        }
        if( f.getDeclaringClass().getName().equals("enegade.uml.selector.MethodSelector") ) {
            for( String methodName: new String[] {"selected"} ) {
                if( f.getName().equals(methodName) ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void requestChanges(List<TypeSelector> typeSelectors)
    {
        Path path = Paths.get(this.jsonPath);
        if( Files.exists(path) ) {
            return;
        }

        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }

                    public boolean shouldSkipField(FieldAttributes f) {
                        return BaseChangeMapper.this.shouldSkipField(f);
                    }
                })
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(typeSelectors);


        try ( PrintWriter printWriter = new PrintWriter( new FileWriter(this.jsonPath) ) ) {
            printWriter.print(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TypeSelector> mapChanges()
    {
        Path path = Paths.get(this.jsonPath);

        String json;
        try ( Stream<String> lines = Files.lines(path) ) {
            json = lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        Type mappedType = new TypeToken<List<TypeSelector>>(){}.getType();

        return gson.fromJson(json, mappedType);
    }
}
