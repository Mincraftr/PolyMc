/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.resource;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.swordglowsblue.artifice.api.Artifice;
import io.github.theepicblock.polymc.PolyMc;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class copies all assets into a temp folder. Then uses that to generate the resourcepack, instead of getting the assets straight from the jars.
 * This is slower, but could help with finding assets.
 */
public class AdvancedResourcePackMaker extends ResourcePackMaker{
    protected final Path tempLocation;
    public AdvancedResourcePackMaker(Path buildLocation, Path tempLocation) {
        super(buildLocation);
        this.tempLocation = tempLocation;

//        Path assetTemp = tempLocation.resolve("assets").toAbsolutePath();
        FabricLoader.getInstance().getAllMods().forEach((mod) -> {
            Path assets = mod.getPath("assets");
            if (!Files.exists(assets)) return;
            try {
                System.out.println("Temp dir: " + tempLocation.toString());
                copyAll(assets,tempLocation);
            } catch (IOException e) {
                PolyMc.LOGGER.warn("Failed to get resources from mod " + mod.getMetadata().getId());
                e.printStackTrace();
            }
        });

//        Optional<ModContainer> artifice = FabricLoader.getInstance().getModContainer("artifice");
//        if (artifice.isPresent()) {
//            Artifice.ASSETS.forEach((artificeResourcePack -> {
//                try {
//                    artificeResourcePack.dumpResources(tempLocation.toString());
//                } catch (IOException e) {
//                    PolyMc.LOGGER.warn("Failed to get resources from artifice pack " + artificeResourcePack.getName());
//                    PolyMc.LOGGER.warn(e);
//                }
//            }));
//        }
    }

    public void copyAll(Path from, Path to) throws IOException {
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!attrs.isDirectory()) {
                    Path dest = to.resolve("."+file.toString()); //the dot is needed to make this relative TODO check if this works on windows
                    //noinspection ResultOfMethodCallIgnored
                    dest.toFile().mkdirs();
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                return super.visitFile(file, attrs);
            }
        };

        Files.walkFileTree(from,visitor);
    }

    /**
     * copies a file from the temp file into this resourcepack
     * @param modId
     * @param path example: "asset/testmod/models/item/testitem.json"
     * @return The path to the new file
     */
    @Override
    protected Path copyFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return null;

        Path filePath = tempLocation.resolve(path);
        Path newLoc = BuildLocation.resolve(path);
        boolean c = newLoc.toFile().mkdirs();
        try {
            return Files.copy(filePath, newLoc, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Failed to get resource from mod '"+modId+"' path: " + path);
        }
        return null;
    }

    @Override
    protected boolean checkFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return false;

        Path filePath = tempLocation.resolve(path);
        return Files.exists(filePath);
    }

    /**
     * get's a file from the temp file.
     * @param modId the mod who's assets we're getting from
     * @param path example "asset/testmod/models/item/testitem.json"
     * @return A reader for this file.
     */
    @Override
    protected InputStreamReader getFileFromMod(String modId, String path) {
        if (modId.equals("minecraft")) return null;

        Path filePath = tempLocation.resolve(path);
        try {
            return new InputStreamReader(Files.newInputStream(filePath, StandardOpenOption.READ));
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Failed to get resource from mod '"+modId+"' path: " + path);
        }
        return null;
    }

    @Override
    public void saveAll() {
        super.saveAll();

        try {
            Files.delete(tempLocation);
        } catch (IOException e) {
            PolyMc.LOGGER.warn("Couldn't delete temporary file");
            PolyMc.LOGGER.warn(e);
        }
    }
}