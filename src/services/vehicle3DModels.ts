import * as THREE from 'three';
import { VehicleType } from './vehicleCustomization';

/**
 * Creates a ground ambient occlusion shadow plane
 */
function createGroundShadow(width: number, length: number): THREE.Mesh {
  const canvas = document.createElement('canvas');
  canvas.width = 128;
  canvas.height = 128;
  const ctx = canvas.getContext('2d');
  if (ctx) {
    const grad = ctx.createRadialGradient(64, 64, 10, 64, 64, 60);
    grad.addColorStop(0, 'rgba(0,0,0,0.7)');
    grad.addColorStop(0.5, 'rgba(0,0,0,0.35)');
    grad.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 128, 128);
  }
  const texture = new THREE.CanvasTexture(canvas);
  const geo = new THREE.PlaneGeometry(width * 1.3, length * 1.2);
  const mat = new THREE.MeshBasicMaterial({
    map: texture,
    transparent: true,
    depthWrite: false,
    opacity: 0.8,
  });
  const mesh = new THREE.Mesh(geo, mat);
  mesh.rotation.x = -Math.PI / 2;
  mesh.position.y = 0.02; // Just above road
  return mesh;
}

/**
 * Creates a wheel mesh
 */
function createWheel(radius: number, width: number, rimColor: number = 0xcccccc): THREE.Group {
  const group = new THREE.Group();
  
  // Tire
  const tireGeo = new THREE.CylinderGeometry(radius, radius, width, 16);
  const tireMat = new THREE.MeshStandardMaterial({
    color: 0x111111,
    roughness: 0.9,
    metalness: 0.1,
  });
  const tire = new THREE.Mesh(tireGeo, tireMat);
  tire.rotation.z = Math.PI / 2;
  group.add(tire);

  // Rim
  const rimGeo = new THREE.CylinderGeometry(radius * 0.65, radius * 0.65, width * 1.05, 12);
  const rimMat = new THREE.MeshStandardMaterial({
    color: rimColor,
    roughness: 0.3,
    metalness: 0.8,
  });
  const rim = new THREE.Mesh(rimGeo, rimMat);
  rim.rotation.z = Math.PI / 2;
  group.add(rim);

  return group;
}

/**
 * Creates a soft volumetric beam texture fading to 0 at the far end
 */
function createBeamVolumeTexture(): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = 64;
  canvas.height = 256;
  const ctx = canvas.getContext('2d');
  if (ctx) {
    // Vertical gradient: y=0 (headlight, bright) to y=256 (far end, 100% transparent)
    const grad = ctx.createLinearGradient(0, 0, 0, 256);
    grad.addColorStop(0, 'rgba(255, 255, 230, 0.65)');
    grad.addColorStop(0.2, 'rgba(255, 250, 200, 0.45)');
    grad.addColorStop(0.5, 'rgba(255, 245, 180, 0.20)');
    grad.addColorStop(0.8, 'rgba(255, 240, 160, 0.05)');
    grad.addColorStop(1.0, 'rgba(255, 240, 160, 0.0)');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 64, 256);
  }
  return new THREE.CanvasTexture(canvas);
}

/**
 * Creates an asphalt road illumination texture projected forward from the vehicle
 */
function createRoadLightTexture(isSingle: boolean): THREE.CanvasTexture {
  const canvas = document.createElement('canvas');
  canvas.width = 512;
  canvas.height = 512;
  const ctx = canvas.getContext('2d');
  if (ctx) {
    ctx.clearRect(0, 0, 512, 512);

    if (isSingle) {
      // Single central beam (motorcycle)
      // Starts narrow at y=10 (front bumper), expands down to y=490 (road ahead)
      const grad = ctx.createRadialGradient(256, 35, 15, 256, 260, 280);
      grad.addColorStop(0, 'rgba(255, 255, 220, 0.85)');
      grad.addColorStop(0.25, 'rgba(255, 250, 190, 0.55)');
      grad.addColorStop(0.6, 'rgba(255, 242, 170, 0.22)');
      grad.addColorStop(0.85, 'rgba(255, 238, 150, 0.06)');
      grad.addColorStop(1.0, 'rgba(255, 238, 150, 0)');

      ctx.beginPath();
      ctx.moveTo(256 - 15, 10);
      ctx.lineTo(256 + 15, 10);
      ctx.lineTo(256 + 160, 500);
      ctx.lineTo(256 - 160, 500);
      ctx.closePath();
      ctx.fillStyle = grad;
      ctx.fill();
    } else {
      // Dual headlights (cars)
      const leftX = 256 - 45;
      const rightX = 256 + 45;

      // Left beam cone
      const gradL = ctx.createRadialGradient(leftX, 35, 10, leftX - 30, 240, 260);
      gradL.addColorStop(0, 'rgba(255, 255, 220, 0.85)');
      gradL.addColorStop(0.22, 'rgba(255, 250, 190, 0.55)');
      gradL.addColorStop(0.55, 'rgba(255, 242, 170, 0.22)');
      gradL.addColorStop(0.85, 'rgba(255, 238, 150, 0.06)');
      gradL.addColorStop(1.0, 'rgba(255, 238, 150, 0)');

      ctx.beginPath();
      ctx.moveTo(leftX - 12, 10);
      ctx.lineTo(leftX + 12, 10);
      ctx.lineTo(leftX + 80, 500);
      ctx.lineTo(leftX - 140, 500);
      ctx.closePath();
      ctx.fillStyle = gradL;
      ctx.fill();

      // Right beam cone
      const gradR = ctx.createRadialGradient(rightX, 35, 10, rightX + 30, 240, 260);
      gradR.addColorStop(0, 'rgba(255, 255, 220, 0.85)');
      gradR.addColorStop(0.22, 'rgba(255, 250, 190, 0.55)');
      gradR.addColorStop(0.55, 'rgba(255, 242, 170, 0.22)');
      gradR.addColorStop(0.85, 'rgba(255, 238, 150, 0.06)');
      gradR.addColorStop(1.0, 'rgba(255, 238, 150, 0)');

      ctx.beginPath();
      ctx.moveTo(rightX - 12, 10);
      ctx.lineTo(rightX + 12, 10);
      ctx.lineTo(rightX + 140, 500);
      ctx.lineTo(rightX - 80, 500);
      ctx.closePath();
      ctx.fillStyle = gradR;
      ctx.fill();

      // Soft center light pool where beams merge
      const gradCenter = ctx.createRadialGradient(256, 180, 20, 256, 260, 220);
      gradCenter.addColorStop(0, 'rgba(255, 250, 200, 0.45)');
      gradCenter.addColorStop(0.5, 'rgba(255, 245, 180, 0.20)');
      gradCenter.addColorStop(1.0, 'rgba(255, 240, 170, 0)');
      ctx.fillStyle = gradCenter;
      ctx.fillRect(0, 0, 512, 512);
    }
  }
  return new THREE.CanvasTexture(canvas);
}

/**
 * Creates realistic forward-projecting volumetric headlight beams and ground road illumination
 */
function createHeadlightBeams(
  spread: number,
  length: number,
  frontZ: number = 2.3,
  heightY: number = 0.55,
  isSingle: boolean = false
): THREE.Group {
  const group = new THREE.Group();
  const roadY = 0.03;

  // 1. Road surface illumination projector (asphalt light pool)
  const roadLength = Math.max(length * 1.6, 9.0);
  const roadWidth = Math.max(spread * 2.8, 4.2);
  const roadPlaneGeo = new THREE.PlaneGeometry(roadWidth, roadLength);
  const roadPlaneMat = new THREE.MeshBasicMaterial({
    map: createRoadLightTexture(isSingle),
    transparent: true,
    opacity: 0.75,
    depthWrite: false,
    side: THREE.FrontSide,
  });
  const roadPlane = new THREE.Mesh(roadPlaneGeo, roadPlaneMat);
  roadPlane.rotation.x = -Math.PI / 2;
  roadPlane.position.set(0, 0.025, frontZ + roadLength / 2);
  group.add(roadPlane);

  // 2. Volumetric 3D beam cones fading smoothly into air
  const beamLength = Math.min(length, 6.0);
  const beamRadiusStart = isSingle ? 0.08 : 0.05;
  const beamRadiusEnd = isSingle ? spread * 0.75 : spread * 0.45;

  const beamGeo = new THREE.CylinderGeometry(
    beamRadiusStart,
    beamRadiusEnd,
    beamLength,
    16,
    1,
    true
  );
  // Shift local geometry so (0,0,0) is at the narrow top cap (headlight bulb)
  beamGeo.translate(0, -beamLength / 2, 0);

  const beamMat = new THREE.MeshBasicMaterial({
    map: createBeamVolumeTexture(),
    transparent: true,
    opacity: 0.45,
    depthWrite: false,
    side: THREE.DoubleSide,
  });

  const vDown = new THREE.Vector3(0, -1, 0);

  if (isSingle) {
    const dir = new THREE.Vector3(0, roadY - heightY, beamLength).normalize();
    const q = new THREE.Quaternion().setFromUnitVectors(vDown, dir);
    const beam = new THREE.Mesh(beamGeo, beamMat);
    beam.setRotationFromQuaternion(q);
    beam.position.set(0, heightY, frontZ);
    group.add(beam);
  } else {
    // Left beam
    const leftX = -spread * 0.48;
    const dirLeft = new THREE.Vector3(-0.2, roadY - heightY, beamLength).normalize();
    const qLeft = new THREE.Quaternion().setFromUnitVectors(vDown, dirLeft);
    const leftBeam = new THREE.Mesh(beamGeo, beamMat);
    leftBeam.setRotationFromQuaternion(qLeft);
    leftBeam.position.set(leftX, heightY, frontZ);
    group.add(leftBeam);

    // Right beam
    const rightX = spread * 0.48;
    const dirRight = new THREE.Vector3(0.2, roadY - heightY, beamLength).normalize();
    const qRight = new THREE.Quaternion().setFromUnitVectors(vDown, dirRight);
    const rightBeam = new THREE.Mesh(beamGeo, beamMat);
    rightBeam.setRotationFromQuaternion(qRight);
    rightBeam.position.set(rightX, heightY, frontZ);
    group.add(rightBeam);
  }

  return group;
}

/**
 * Generates a full 3D vehicle mesh hierarchy based on type and primary color
 */
export function buildVehicle3D(
  type: VehicleType,
  primaryColorHex: string,
  showHeadlights: boolean = true
): THREE.Group {
  const root = new THREE.Group();
  const color = new THREE.Color(primaryColorHex);
  const darkColor = color.clone().multiplyScalar(0.4);
  const lightColor = color.clone().lerp(new THREE.Color(0xffffff), 0.3);

  // Materials
  const bodyMaterial = new THREE.MeshStandardMaterial({
    color: color,
    roughness: 0.25,
    metalness: 0.7,
  });

  const bodyAccentMaterial = new THREE.MeshStandardMaterial({
    color: darkColor,
    roughness: 0.4,
    metalness: 0.5,
  });

  const glassMaterial = new THREE.MeshStandardMaterial({
    color: 0x112233,
    roughness: 0.1,
    metalness: 0.9,
    transparent: true,
    opacity: 0.85,
  });

  const carbonMaterial = new THREE.MeshStandardMaterial({
    color: 0x151515,
    roughness: 0.6,
    metalness: 0.3,
  });

  const headlightMaterial = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    emissive: 0xffffff,
    emissiveIntensity: 2.0,
  });

  const taillightMaterial = new THREE.MeshStandardMaterial({
    color: 0xff0022,
    emissive: 0xff0022,
    emissiveIntensity: 2.5,
  });

  switch (type) {
    case 'car_sport': {
      // Dimensions: 2.1m wide, 1.25m high, 4.7m long
      root.add(createGroundShadow(2.4, 5.0));

      // Lower Chassis & Diffuser
      const lowerChassis = new THREE.Mesh(
        new THREE.BoxGeometry(2.1, 0.4, 4.6),
        bodyAccentMaterial
      );
      lowerChassis.position.y = 0.35;
      root.add(lowerChassis);

      // Main Sculpted Body
      const mainBody = new THREE.Mesh(
        new THREE.BoxGeometry(2.0, 0.4, 4.4),
        bodyMaterial
      );
      mainBody.position.y = 0.55;
      root.add(mainBody);

      // Hood Slant
      const hood = new THREE.Mesh(
        new THREE.BoxGeometry(1.85, 0.25, 1.6),
        bodyMaterial
      );
      hood.position.set(0, 0.65, 1.2);
      hood.rotation.x = -0.1;
      root.add(hood);

      // Cockpit Greenhouse (Glass cabin)
      const cabin = new THREE.Mesh(
        new THREE.BoxGeometry(1.5, 0.48, 2.0),
        glassMaterial
      );
      cabin.position.set(0, 0.95, -0.2);
      root.add(cabin);

      // Roof panel
      const roof = new THREE.Mesh(
        new THREE.BoxGeometry(1.4, 0.08, 1.6),
        bodyMaterial
      );
      roof.position.set(0, 1.22, -0.2);
      root.add(roof);

      // Rear Spoiler / Wing
      const wingStrutLeft = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.35, 0.15), carbonMaterial);
      wingStrutLeft.position.set(-0.7, 0.9, -2.0);
      root.add(wingStrutLeft);

      const wingStrutRight = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.35, 0.15), carbonMaterial);
      wingStrutRight.position.set(0.7, 0.9, -2.0);
      root.add(wingStrutRight);

      const wing = new THREE.Mesh(new THREE.BoxGeometry(1.9, 0.06, 0.4), carbonMaterial);
      wing.position.set(0, 1.08, -2.0);
      wing.rotation.x = -0.05;
      root.add(wing);

      // Headlights LED
      const hlLeft = new THREE.Mesh(new THREE.BoxGeometry(0.35, 0.08, 0.05), headlightMaterial);
      hlLeft.position.set(-0.75, 0.55, 2.3);
      hlLeft.rotation.y = 0.2;
      root.add(hlLeft);

      const hlRight = new THREE.Mesh(new THREE.BoxGeometry(0.35, 0.08, 0.05), headlightMaterial);
      hlRight.position.set(0.75, 0.55, 2.3);
      hlRight.rotation.y = -0.2;
      root.add(hlRight);

      // Taillight Full-width LED bar
      const tailLight = new THREE.Mesh(new THREE.BoxGeometry(1.85, 0.08, 0.05), taillightMaterial);
      tailLight.position.set(0, 0.65, -2.31);
      root.add(tailLight);

      // 4 Wheels
      const wFL = createWheel(0.36, 0.26);
      wFL.position.set(-1.02, 0.36, 1.4);
      root.add(wFL);

      const wFR = createWheel(0.36, 0.26);
      wFR.position.set(1.02, 0.36, 1.4);
      root.add(wFR);

      const wRL = createWheel(0.38, 0.32);
      wRL.position.set(-1.04, 0.38, -1.4);
      root.add(wRL);

      const wRR = createWheel(0.38, 0.32);
      wRR.position.set(1.04, 0.38, -1.4);
      root.add(wRR);

      if (showHeadlights) {
        root.add(createHeadlightBeams(1.5, 5.5, 2.3, 0.55));
      }
      break;
    }

    case 'car_f1': {
      // Dimensions: 2.0m wide, 1.0m high, 5.2m long
      root.add(createGroundShadow(2.3, 5.4));

      // Slender Body / Monocoque
      const body = new THREE.Mesh(
        new THREE.BoxGeometry(0.7, 0.35, 3.8),
        bodyMaterial
      );
      body.position.set(0, 0.35, 0);
      root.add(body);

      // Long Tapered Nose
      const nose = new THREE.Mesh(
        new THREE.ConeGeometry(0.35, 1.6, 4),
        bodyMaterial
      );
      nose.rotation.x = Math.PI / 2;
      nose.rotation.y = Math.PI / 4;
      nose.position.set(0, 0.3, 2.5);
      root.add(nose);

      // Front Multi-element Wing
      const frontWing = new THREE.Mesh(
        new THREE.BoxGeometry(1.9, 0.05, 0.45),
        carbonMaterial
      );
      frontWing.position.set(0, 0.16, 2.45);
      root.add(frontWing);

      // Sidepods
      const podL = new THREE.Mesh(new THREE.BoxGeometry(0.45, 0.35, 1.8), bodyMaterial);
      podL.position.set(-0.6, 0.32, -0.3);
      root.add(podL);

      const podR = new THREE.Mesh(new THREE.BoxGeometry(0.45, 0.35, 1.8), bodyMaterial);
      podR.position.set(0.6, 0.32, -0.3);
      root.add(podR);

      // Halo Safety Ring
      const halo = new THREE.Mesh(
        new THREE.TorusGeometry(0.3, 0.04, 8, 16, Math.PI),
        carbonMaterial
      );
      halo.rotation.x = Math.PI / 2;
      halo.position.set(0, 0.72, 0.2);
      root.add(halo);

      // Airbox Engine Intake above driver
      const airbox = new THREE.Mesh(
        new THREE.BoxGeometry(0.3, 0.35, 0.8),
        bodyMaterial
      );
      airbox.position.set(0, 0.75, -0.5);
      root.add(airbox);

      // Rear Downforce Wing
      const rearWing = new THREE.Mesh(
        new THREE.BoxGeometry(1.6, 0.25, 0.3),
        carbonMaterial
      );
      rearWing.position.set(0, 0.85, -2.1);
      root.add(rearWing);

      // Rear Red Rain Light
      const rainLight = new THREE.Mesh(
        new THREE.BoxGeometry(0.12, 0.12, 0.05),
        taillightMaterial
      );
      rainLight.position.set(0, 0.3, -2.2);
      root.add(rainLight);

      // 4 Large Exposed Slick Tires
      const tireFrontL = createWheel(0.34, 0.32, 0xf59e0b);
      tireFrontL.position.set(-0.95, 0.34, 1.6);
      root.add(tireFrontL);

      const tireFrontR = createWheel(0.34, 0.32, 0xf59e0b);
      tireFrontR.position.set(0.95, 0.34, 1.6);
      root.add(tireFrontR);

      const tireRearL = createWheel(0.36, 0.42, 0xf59e0b);
      tireRearL.position.set(-1.0, 0.36, -1.6);
      root.add(tireRearL);

      const tireRearR = createWheel(0.36, 0.42, 0xf59e0b);
      tireRearR.position.set(1.0, 0.36, -1.6);
      root.add(tireRearR);

      if (showHeadlights) {
        root.add(createHeadlightBeams(1.2, 6.0, 2.45, 0.35));
      }
      break;
    }

    case 'car_cyber': {
      // Futuristic wedge hovercar
      root.add(createGroundShadow(2.4, 4.8));

      // Sculpted sharp hull
      const hullGeo = new THREE.ConeGeometry(1.4, 4.5, 5);
      const hull = new THREE.Mesh(hullGeo, bodyMaterial);
      hull.rotation.x = Math.PI / 2;
      hull.rotation.y = Math.PI;
      hull.position.set(0, 0.5, 0);
      root.add(hull);

      // Cockpit canopy
      const cyberCabin = new THREE.Mesh(
        new THREE.BoxGeometry(1.2, 0.4, 1.8),
        new THREE.MeshStandardMaterial({
          color: 0x00f0ff,
          emissive: 0x005577,
          roughness: 0.1,
          metalness: 0.9,
          transparent: true,
          opacity: 0.9,
        })
      );
      cyberCabin.position.set(0, 0.75, -0.1);
      root.add(cyberCabin);

      // 4 Plasma Anti-Grav Thruster Pods (Hovering)
      const thrusterMat = new THREE.MeshStandardMaterial({
        color: 0x00ffff,
        emissive: 0x00ffff,
        emissiveIntensity: 3.0,
      });

      const positions = [
        [-0.9, 0.3, 1.3],
        [0.9, 0.3, 1.3],
        [-0.95, 0.3, -1.3],
        [0.95, 0.3, -1.3],
      ];

      positions.forEach(([x, y, z]) => {
        const pod = new THREE.Mesh(new THREE.CylinderGeometry(0.28, 0.28, 0.25, 12), carbonMaterial);
        pod.position.set(x, y, z);
        root.add(pod);

        const ring = new THREE.Mesh(new THREE.TorusGeometry(0.25, 0.05, 8, 16), thrusterMat);
        ring.rotation.x = Math.PI / 2;
        ring.position.set(x, y - 0.12, z);
        root.add(ring);
      });

      // Cyber Neon Headlight strip
      const neonFront = new THREE.Mesh(
        new THREE.BoxGeometry(1.6, 0.06, 0.06),
        new THREE.MeshStandardMaterial({ color: 0x00ffff, emissive: 0x00ffff, emissiveIntensity: 3.5 })
      );
      neonFront.position.set(0, 0.45, 2.2);
      root.add(neonFront);

      // Cyber Neon Taillight
      const neonRear = new THREE.Mesh(
        new THREE.BoxGeometry(1.8, 0.06, 0.06),
        new THREE.MeshStandardMaterial({ color: 0xff0055, emissive: 0xff0055, emissiveIntensity: 3.5 })
      );
      neonRear.position.set(0, 0.55, -2.2);
      root.add(neonRear);

      if (showHeadlights) {
        root.add(createHeadlightBeams(1.4, 5.5, 2.2, 0.45));
      }
      break;
    }

    case 'car_muscle': {
      // Classic Muscle Car V8
      root.add(createGroundShadow(2.3, 5.0));

      // Main Boxy Muscular Body
      const body = new THREE.Mesh(new THREE.BoxGeometry(2.05, 0.5, 4.7), bodyMaterial);
      body.position.y = 0.55;
      root.add(body);

      // Hood Blower / Supercharger Scoop
      const scoop = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.2, 0.9), carbonMaterial);
      scoop.position.set(0, 0.85, 1.1);
      root.add(scoop);

      // Fastback Cabin
      const cabin = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.5, 2.1), glassMaterial);
      cabin.position.set(0, 1.0, -0.3);
      root.add(cabin);

      // Roof
      const roof = new THREE.Mesh(new THREE.BoxGeometry(1.5, 0.08, 1.8), bodyMaterial);
      roof.position.set(0, 1.26, -0.3);
      root.add(roof);

      // Quad Round Headlights
      [-0.75, -0.45, 0.45, 0.75].forEach(x => {
        const hl = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.1, 0.05, 12), headlightMaterial);
        hl.rotation.x = Math.PI / 2;
        hl.position.set(x, 0.6, 2.36);
        root.add(hl);
      });

      // Triple Rear Taillights
      const tail = new THREE.Mesh(new THREE.BoxGeometry(1.7, 0.12, 0.05), taillightMaterial);
      tail.position.set(0, 0.65, -2.36);
      root.add(tail);

      // 4 Muscle Wheels
      const wFL = createWheel(0.38, 0.3, 0xdddddd);
      wFL.position.set(-1.02, 0.38, 1.4);
      root.add(wFL);
      const wFR = createWheel(0.38, 0.3, 0xdddddd);
      wFR.position.set(1.02, 0.38, 1.4);
      root.add(wFR);
      const wRL = createWheel(0.42, 0.36, 0xdddddd);
      wRL.position.set(-1.03, 0.42, -1.35);
      root.add(wRL);
      const wRR = createWheel(0.42, 0.36, 0xdddddd);
      wRR.position.set(1.03, 0.42, -1.35);
      root.add(wRR);

      if (showHeadlights) {
        root.add(createHeadlightBeams(1.5, 5.5, 2.36, 0.6));
      }
      break;
    }

    case 'car_suv': {
      // Rugged 4x4 Offroad
      root.add(createGroundShadow(2.4, 5.0));

      // Raised Chassis
      const body = new THREE.Mesh(new THREE.BoxGeometry(2.1, 0.7, 4.6), bodyMaterial);
      body.position.y = 0.85;
      root.add(body);

      // Tall Cabin
      const cabin = new THREE.Mesh(new THREE.BoxGeometry(1.8, 0.65, 2.8), glassMaterial);
      cabin.position.set(0, 1.45, -0.3);
      root.add(cabin);

      // Roof Expedition Rack
      const rack = new THREE.Mesh(new THREE.BoxGeometry(1.7, 0.12, 2.5), carbonMaterial);
      rack.position.set(0, 1.84, -0.3);
      root.add(rack);

      // Roof Quad Floodlights
      const floodlights = new THREE.Mesh(new THREE.BoxGeometry(1.4, 0.1, 0.1), headlightMaterial);
      floodlights.position.set(0, 1.88, 0.95);
      root.add(floodlights);

      // Rear Spare Tire
      const spareTire = createWheel(0.4, 0.3, 0x222222);
      spareTire.rotation.y = Math.PI / 2;
      spareTire.position.set(0, 0.9, -2.45);
      root.add(spareTire);

      // 4 Chunky Lifted Offroad Wheels
      const wFL = createWheel(0.44, 0.35, 0x333333);
      wFL.position.set(-1.05, 0.44, 1.4);
      root.add(wFL);
      const wFR = createWheel(0.44, 0.35, 0x333333);
      wFR.position.set(1.05, 0.44, 1.4);
      root.add(wFR);
      const wRL = createWheel(0.44, 0.35, 0x333333);
      wRL.position.set(-1.05, 0.44, -1.4);
      root.add(wRL);
      const wRR = createWheel(0.44, 0.35, 0x333333);
      wRR.position.set(1.05, 0.44, -1.4);
      root.add(wRR);

      if (showHeadlights) {
        root.add(createHeadlightBeams(1.6, 6.0, 2.3, 0.85));
      }
      break;
    }

    case 'car_moto': {
      // Superbike GP
      root.add(createGroundShadow(1.2, 3.2));

      // Slim Fairing & Tank
      const frame = new THREE.Mesh(new THREE.BoxGeometry(0.45, 0.6, 1.8), bodyMaterial);
      frame.position.set(0, 0.65, 0.1);
      root.add(frame);

      // Driver Helmet / Rider Silhouette
      const helmet = new THREE.Mesh(
        new THREE.SphereGeometry(0.24, 12, 12),
        new THREE.MeshStandardMaterial({ color: 0x111111, roughness: 0.2, metalness: 0.8 })
      );
      helmet.position.set(0, 1.25, -0.2);
      root.add(helmet);

      // Front Headlight
      const hl = new THREE.Mesh(new THREE.BoxGeometry(0.25, 0.12, 0.05), headlightMaterial);
      hl.position.set(0, 0.7, 1.1);
      root.add(hl);

      // Front and Rear Wheels (Tandem)
      const wF = createWheel(0.34, 0.18, 0x111111);
      wF.position.set(0, 0.34, 1.15);
      root.add(wF);

      const wR = createWheel(0.35, 0.24, 0x111111);
      wR.position.set(0, 0.35, -1.05);
      root.add(wR);

      if (showHeadlights) {
        root.add(createHeadlightBeams(0.8, 5.0, 1.1, 0.7, true));
      }
      break;
    }

    case 'arrow_gta':
    case 'arrow_waze': {
      // Volumetric 3D Faceted Radar Arrow
      root.add(createGroundShadow(2.2, 2.8));

      // 3D Prism Arrow Geometry
      const arrowShape = new THREE.Shape();
      arrowShape.moveTo(0, 1.8);
      arrowShape.lineTo(1.2, -1.0);
      arrowShape.lineTo(0.4, -0.6);
      arrowShape.lineTo(0, -0.3);
      arrowShape.lineTo(-0.4, -0.6);
      arrowShape.lineTo(-1.2, -1.0);
      arrowShape.closePath();

      const extrudeSettings = {
        depth: 0.45,
        bevelEnabled: true,
        bevelSegments: 3,
        steps: 1,
        bevelSize: 0.12,
        bevelThickness: 0.12,
      };

      const arrowGeo = new THREE.ExtrudeGeometry(arrowShape, extrudeSettings);
      const arrowMesh = new THREE.Mesh(
        arrowGeo,
        new THREE.MeshStandardMaterial({
          color: color,
          emissive: lightColor,
          emissiveIntensity: 0.4,
          roughness: 0.2,
          metalness: 0.8,
        })
      );
      arrowMesh.rotation.x = Math.PI / 2;
      arrowMesh.position.set(0, 0.5, 0);
      root.add(arrowMesh);

      if (showHeadlights) {
        root.add(createHeadlightBeams(1.2, 4.5, 1.8, 0.5));
      }
      break;
    }

    case 'minecraft_arrow':
    default: {
      // 3D Voxel Pixel Cursor
      root.add(createGroundShadow(2.0, 2.5));

      const voxelMat = new THREE.MeshStandardMaterial({
        color: 0xffffff,
        roughness: 0.5,
        metalness: 0.1,
      });
      const borderMat = new THREE.MeshStandardMaterial({
        color: 0x111111,
        roughness: 0.8,
        metalness: 0.1,
      });

      const core = new THREE.Mesh(new THREE.ConeGeometry(1.0, 2.2, 4), voxelMat);
      core.rotation.x = Math.PI / 2;
      core.rotation.y = Math.PI / 4;
      core.position.set(0, 0.6, 0);
      root.add(core);

      const outline = new THREE.Mesh(new THREE.ConeGeometry(1.15, 2.3, 4), borderMat);
      outline.rotation.x = Math.PI / 2;
      outline.rotation.y = Math.PI / 4;
      outline.position.set(0, 0.55, 0);
      root.add(outline);
      break;
    }
  }

  return root;
}
