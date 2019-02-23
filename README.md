# Anchor Engine

A game engine with a level editor and (incomplete and buggy) game. It was written for Artifex 2018.

## Features
* Physically based entity and terrain renderer
* Screen space reflections
* Screen space ambient occlusion
* Image based lighting
* Bloom
* HDR
* Volumetric Scattering
* Fast approximate anti-aliasing
* Variance Shadow Maps
* Cascaded Shadow Maps
* Particle Renderer
* Screen space deferred decals
* Font and GUI renderer
* Profiler and developer console

## Screenshots

![Level editor](https://i.gyazo.com/7098149793f0a5ef458b8ee9095241b8.png)

## Setup

Each folder should be a separate repository in Eclipse. It would help if the repositories are all in the same working set.

### Dependencies

* LWJGL v2.9.3
  * lwjgl.jar
  * lwjgl_util.jar
* trident-1.5.00.jar
* [SubstanceUI](https://github.com/dentmaged/substance)

* Client
  * lwjgl.jar and lwjgl_util.jar
  * Common
  * Engine
  * Renderer
* Common
  * lwjgl_util.jar only
* Editor
  * lwjgl.jar and lwjgl_util.jar
  * trident-1.5.00.jar
  * Client
  * Common
  * Engine
  * Renderer
  * SubstanceUI
* Engine
  * lwjgl_util.jar
  * Common
* Renderer
  * lwjgl.jar and lwjgl_util.jar
  * Common
* Server
  * lwjgl_util.jar only
  * Common
  * Engine

## How to run

There are three main classes:
* `GameServer`: `org.anchor.game.server.GameServer`
* `GameClient`: `org.anchor.game.client.GameClient`
* `GameEditor`: `org.anchor.game.editor.GameEditor`

## Information

* Not all maps in the `dev_maps` folder will load. Many of them are old and they will crash the editor upon loading. Some will have buggy lighting.

