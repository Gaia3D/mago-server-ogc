const magoSelection = {
    screenSpaceEventHandler: undefined,
    pickedObject: undefined,
    selectionColor: "#abd2ff",
}

const onObjectSelection = (viewer, selectEvent, deselectEvent) => {
    const scene = viewer.scene;
    const canvas = viewer.canvas;

    let handler;
    if (!magoSelection.screenSpaceEventHandler) {
        handler = new Cesium.ScreenSpaceEventHandler(scene.canvas);
        magoSelection.screenSpaceEventHandler = handler;
    } else {
        handler = magoSelection.screenSpaceEventHandler;
    }

    const mouseDownHandler = (event) => {
        if (!deselectObject(deselectEvent)) {
            if (!selectObject(event, selectEvent)) {
                return;
            }
        }
        offObjectSelection(viewer);
    }

    let recentPickedObject = undefined;
    const mouseMoveHandler = (moveEvent) => {
        const tempPickedObject = scene.pick(moveEvent.endPosition);
        if (tempPickedObject && tempPickedObject === magoSelection.pickedObject) {
            return;
        }

        if (tempPickedObject instanceof Cesium.Cesium3DTileFeature) {
            const tileset = tempPickedObject.tileset;
            tileset.style = new Cesium.Cesium3DTileStyle({
                color: "color('" + magoSelection.selectionColor + "', 0.9)",
            });
            recentPickedObject = tempPickedObject;
            viewer.canvas.style.cursor = "pointer";
        } else if (tempPickedObject?.content instanceof Cesium.Model3DTileContent) {
            const tileset = tempPickedObject.content.tileset;
            tileset.style = new Cesium.Cesium3DTileStyle({
                color: "color('" + magoSelection.selectionColor + "', 0.9)",
            });
            recentPickedObject = tempPickedObject.content;
            viewer.canvas.style.cursor = "pointer";
        } else {
            if (recentPickedObject && recentPickedObject instanceof Cesium.Cesium3DTileFeature) {
                const tileset = recentPickedObject.tileset;
                tileset.style = undefined;
                recentPickedObject = undefined;
            } else if (recentPickedObject && recentPickedObject instanceof Cesium.Model3DTileContent) {
                const tileset = recentPickedObject.tileset;
                tileset.style = undefined;
                recentPickedObject = undefined;
            }
            viewer.canvas.style.cursor = "default";
        }
    }

    handler.setInputAction(mouseDownHandler, Cesium.ScreenSpaceEventType.LEFT_CLICK);
    handler.setInputAction(mouseMoveHandler, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
}


const offObjectSelection = (viewer) => {
    const scene = viewer.scene;
    const handler = magoSelection.screenSpaceEventHandler;
    viewer.canvas.style.cursor = "default";
    if (handler) {
        handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_DOWN);
        handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
    }
}

const selectObject = (event, callback) => {
    const scene = viewer.scene;
    magoSelection.pickedObject = scene.pick(event.position);
    if (magoSelection.pickedObject instanceof Cesium.Cesium3DTileFeature) {
        const tileset = magoSelection.pickedObject.tileset;
        tileset.style = new Cesium.Cesium3DTileStyle({
            color: "color('" + magoSelection.selectionColor + "', 0.8)",
        });
        tileset.debugShowBoundingVolume = true;
    } else if (magoSelection.pickedObject?.content instanceof Cesium.Model3DTileContent) {
        const tileset = magoSelection.pickedObject.content.tileset;
        tileset.style = new Cesium.Cesium3DTileStyle({
            color: "color('" + magoSelection.selectionColor + "', 0.8)",
        });
        magoSelection.pickedObject = magoSelection.pickedObject.content;
        tileset.debugShowBoundingVolume = true;
    }

    if (!magoSelection.pickedObject) {
        return false;
    }

    //console.log(magoSelection.pickedObject)
    /*const propertyKeys = magoSelection.pickedObject.getPropertyIds();
    const propertyValues = propertyKeys.map((key) => {
        return magoSelection.pickedObject.getProperty(key);
    });
    propertyValues.forEach((value, index) => {
        console.log(propertyKeys[index] + ": " + value);
    });*/
    //const nodeName = magoSelection.pickedObject.getProperty(magoSelection.pickedObject.getPropertyIds()[0]);
    //console.log(nodeName);

    if (callback) {
        callback();
    }
    return true;
}

const deselectObject = (callback) => {
    if (magoSelection.pickedObject) {
        const scene = viewer.scene;
        if (magoSelection.pickedObject instanceof Cesium.Cesium3DTileFeature) {
            const tileset = magoSelection.pickedObject.tileset;
            tileset.style = undefined;
            tileset.debugShowBoundingVolume = false;
        } else if (magoSelection.pickedObject instanceof Cesium.Model3DTileContent) {
            const tileset = magoSelection.pickedObject.tileset;
            tileset.style = undefined;
            tileset.debugShowBoundingVolume = false;
        }
        magoSelection.pickedObject = undefined;

        if (callback) {
            callback();
        }
        return true;
    } else {
        return false;
    }
}