import * as THREE from 'three';
import {
  MercatorCoordinate,
  CustomLayerInterface,
  CustomRenderMethodInput,
  Map as MapLibreMap,
} from 'maplibre-gl';
import { VehicleType } from './vehicleCustomization';
import { buildVehicle3D } from './vehicle3DModels';
import { Coordinates } from '../types';

export class Vehicle3DLayer implements CustomLayerInterface {
  public id = 'vehicle-3d-model-layer';
  public type = 'custom' as const;
  public renderingMode = '3d' as const;

  private map: MapLibreMap | null = null;
  private gl: WebGLRenderingContext | WebGL2RenderingContext | null = null;
  private scene: THREE.Scene | null = null;
  private camera: THREE.Camera | null = null;
  private renderer: THREE.WebGLRenderer | null = null;
  private vehicleMeshGroup: THREE.Group | null = null;

  private position: Coordinates = [0, 0];
  private heading: number = 0;
  private vehicleType: VehicleType = 'car_sport';
  private vehicleColor: string = '#ef4444';
  private showHeadlights: boolean = true;

  constructor(
    initialPosition: Coordinates,
    initialHeading: number,
    initialType: VehicleType,
    initialColor: string,
    initialShowHeadlights: boolean
  ) {
    this.position = initialPosition;
    this.heading = initialHeading;
    this.vehicleType = initialType;
    this.vehicleColor = initialColor;
    this.showHeadlights = initialShowHeadlights;
  }

  public update(
    position: Coordinates,
    heading: number,
    type: VehicleType,
    color: string,
    showHeadlights: boolean
  ) {
    const meshChanged =
      this.vehicleType !== type ||
      this.vehicleColor !== color ||
      this.showHeadlights !== showHeadlights;

    this.position = position;
    this.heading = heading;
    this.vehicleType = type;
    this.vehicleColor = color;
    this.showHeadlights = showHeadlights;

    if (meshChanged && this.scene) {
      this.rebuildMesh();
    }

    if (this.map) {
      this.map.triggerRepaint();
    }
  }

  private rebuildMesh() {
    if (!this.scene) return;

    if (this.vehicleMeshGroup) {
      this.scene.remove(this.vehicleMeshGroup);
      this.vehicleMeshGroup.traverse((child) => {
        if (child instanceof THREE.Mesh) {
          child.geometry.dispose();
          if (Array.isArray(child.material)) {
            child.material.forEach((m) => m.dispose());
          } else if (child.material) {
            child.material.dispose();
          }
        }
      });
      this.vehicleMeshGroup = null;
    }

    this.vehicleMeshGroup = buildVehicle3D(
      this.vehicleType,
      this.vehicleColor,
      this.showHeadlights
    );
    this.scene.add(this.vehicleMeshGroup);
  }

  public onAdd(map: MapLibreMap, gl: WebGLRenderingContext | WebGL2RenderingContext) {
    this.map = map;
    this.gl = gl;

    this.scene = new THREE.Scene();
    this.camera = new THREE.Camera();

    // Studio lights for realistic car paint reflections
    const ambientLight = new THREE.AmbientLight(0xffffff, 1.4);
    this.scene.add(ambientLight);

    const sunLight = new THREE.DirectionalLight(0xffffff, 2.2);
    sunLight.position.set(25, -40, 50).normalize();
    this.scene.add(sunLight);

    const rimLight = new THREE.DirectionalLight(0x99ccff, 1.2);
    rimLight.position.set(-25, 40, 30).normalize();
    this.scene.add(rimLight);

    this.rebuildMesh();

    this.renderer = new THREE.WebGLRenderer({
      canvas: map.getCanvas(),
      context: gl,
      antialias: true,
    });
    this.renderer.autoClear = false;
  }

  public render(_gl: WebGL2RenderingContext, options: CustomRenderMethodInput) {
    if (!this.map || !this.scene || !this.camera || !this.renderer) return;

    // Convert GPS coordinate to MapLibre Mercator [0..1]
    const coord = MercatorCoordinate.fromLngLat(
      [this.position[0], this.position[1]],
      0
    );

    const baseMeterScale = coord.meterInMercatorCoordinateUnits();

    // Desired visual size on screen (~58px) so the vehicle is prominent and crisp
    const tr = (this.map as any).painter?.transform || (this.map as any).transform;
    const pixelsPerMeter = baseMeterScale * (tr?.worldSize || 1);
    const targetPixelSize = 58;
    const modelLength = 4.8;
    const scaleFactor = targetPixelSize / (modelLength * Math.max(0.0001, pixelsPerMeter));
    const finalScale = baseMeterScale * Math.min(Math.max(scaleFactor, 5.0), 30.0);

    // In MapLibre, mercatorMatrix maps normalized [0..1] Mercator coords to clip space
    const rawMatrix = (tr?.mercatorMatrix || options.modelViewProjectionMatrix) as number[];
    const m = new THREE.Matrix4().fromArray(rawMatrix);

    // Rotation around X axis by 90 deg:
    // Three.js local +Y (Up) -> Mercator +Z (Sky)
    // Three.js local +Z (Forward) -> Mercator +Y (South)
    // Three.js local +X (Right) -> Mercator +X (East)
    const rotationX = new THREE.Matrix4().makeRotationAxis(
      new THREE.Vector3(1, 0, 0),
      Math.PI / 2
    );

    // Heading: 0 = North, 90 = East, 180 = South, 270 = West
    // In Mercator: North is -Y, South is +Y.
    // Since +Z maps to South (+Y), facing North requires 180 deg (Math.PI).
    // Clockwise heading rotates by -headingRad around local Y.
    const headingRad = (this.heading * Math.PI) / 180;
    const rotationHeading = new THREE.Matrix4().makeRotationAxis(
      new THREE.Vector3(0, 1, 0),
      Math.PI - headingRad
    );

    const transform = new THREE.Matrix4()
      .makeTranslation(coord.x, coord.y, coord.z)
      .scale(new THREE.Vector3(finalScale, -finalScale, finalScale))
      .multiply(rotationX)
      .multiply(rotationHeading);

    this.camera.projectionMatrix = m.multiply(transform);

    this.renderer.resetState();
    this.renderer.render(this.scene, this.camera);
  }

  public onRemove() {
    if (this.vehicleMeshGroup && this.scene) {
      this.scene.remove(this.vehicleMeshGroup);
    }
    this.map = null;
    this.gl = null;
  }
}
