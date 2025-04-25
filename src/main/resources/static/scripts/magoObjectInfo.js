const onObjectInfo = (viewer, mouseOnObject, mouseOffObject, outputCallback) => {
  const scene = viewer.scene;

  let handler;
  if (!magoSelection.screenSpaceEventHandler) {
    handler = new Cesium.ScreenSpaceEventHandler(scene.canvas);
    magoSelection.screenSpaceEventHandler = handler;
  } else {
    handler = magoSelection.screenSpaceEventHandler;
  }

  const mouseMoveHandler = (moveEvent) => {
    const tempPickedObject = scene.pick(moveEvent.startPosition);
    if (tempPickedObject instanceof Cesium.Cesium3DTileFeature) {
      if (mouseOnObject) {
        mouseOnObject();
      }
      viewer.canvas.style.cursor = "pointer";
    } else {
      if (mouseOffObject) {
        mouseOffObject();
      }
        viewer.canvas.style.cursor = "default";
    }
  }

  const mouseLeftClickHandler = (event) => {
    const tempPickedObject = scene.pick(event.position);

    if (tempPickedObject instanceof Cesium.Cesium3DTileFeature) {
      const propertyKeys = tempPickedObject.getPropertyIds();
      const propertyValues = propertyKeys.map((key) => {
        return tempPickedObject.getProperty(key);
      });

      const outputValue = [];
      propertyValues.forEach((value, index) => {
        //console.log(propertyKeys[index] + ": " + value);
        outputValue.push({
            key: propertyKeys[index],
            value: value
        });
      });
      outputCallback(outputValue);
    } else {
      outputCallback([]);
      //console.log("Cannot get property values");
    }
  }
  handler.setInputAction(mouseMoveHandler, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
  handler.setInputAction(mouseLeftClickHandler, Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

const offObjectInfo = (viewer) => {
  if (!magoSelection.pickedObject && !Cesium.defined(magoSelection.pickedObject)) {
    return
  }
  const handler = magoSelection.screenSpaceEventHandler;
  if (handler) {
    handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
    handler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
  }
}