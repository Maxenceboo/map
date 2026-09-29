import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';
import { VehicleType } from '../services/vehicleCustomization';
import { buildVehicle3D } from '../services/vehicle3DModels';

interface Vehicle3DPreviewProps {
  vehicleType: VehicleType;
  vehicleColor: string;
  showHeadlights: boolean;
  className?: string;
}

export const Vehicle3DPreview: React.FC<Vehicle3DPreviewProps> = ({
  vehicleType,
  vehicleColor,
  showHeadlights,
  className = '',
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!canvasRef.current || !containerRef.current) return;

    const width = containerRef.current.clientWidth || 300;
    const height = containerRef.current.clientHeight || 180;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(40, width / height, 0.1, 100);
    // Cinematic 3/4 perspective view
    camera.position.set(4.5, 3.2, 5.5);
    camera.lookAt(0, 0.6, 0);

    const renderer = new THREE.WebGLRenderer({
      canvas: canvasRef.current,
      alpha: true,
      antialias: true,
    });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    // Studio lights
    const ambientLight = new THREE.AmbientLight(0xffffff, 1.2);
    scene.add(ambientLight);

    const keyLight = new THREE.DirectionalLight(0xffffff, 2.0);
    keyLight.position.set(5, 8, 5);
    scene.add(keyLight);

    const rimLight = new THREE.DirectionalLight(0x77b5fe, 1.5);
    rimLight.position.set(-5, 4, -5);
    scene.add(rimLight);

    // Build the 3D vehicle
    const vehicleGroup = buildVehicle3D(vehicleType, vehicleColor, showHeadlights);
    scene.add(vehicleGroup);

    let animationFrameId: number;
    let isDragging = false;
    let previousMouseX = 0;
    let rotationVelocity = 0.008; // Gentle auto-turntable spin

    const onPointerDown = (e: PointerEvent) => {
      isDragging = true;
      previousMouseX = e.clientX;
    };

    const onPointerMove = (e: PointerEvent) => {
      if (!isDragging) return;
      const deltaX = e.clientX - previousMouseX;
      previousMouseX = e.clientX;
      vehicleGroup.rotation.y += deltaX * 0.015;
    };

    const onPointerUp = () => {
      isDragging = false;
    };

    const canvas = canvasRef.current;
    canvas.addEventListener('pointerdown', onPointerDown);
    window.addEventListener('pointermove', onPointerMove);
    window.addEventListener('pointerup', onPointerUp);

    const animate = () => {
      animationFrameId = requestAnimationFrame(animate);

      if (!isDragging) {
        vehicleGroup.rotation.y += rotationVelocity;
      }

      renderer.render(scene, camera);
    };

    animate();

    const handleResize = () => {
      if (!containerRef.current) return;
      const w = containerRef.current.clientWidth;
      const h = containerRef.current.clientHeight;
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
      renderer.setSize(w, h);
    };

    window.addEventListener('resize', handleResize);

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('resize', handleResize);
      canvas.removeEventListener('pointerdown', onPointerDown);
      window.removeEventListener('pointermove', onPointerMove);
      window.removeEventListener('pointerup', onPointerUp);

      scene.traverse((child) => {
        if (child instanceof THREE.Mesh) {
          child.geometry.dispose();
          if (Array.isArray(child.material)) {
            child.material.forEach((m) => m.dispose());
          } else if (child.material) {
            child.material.dispose();
          }
        }
      });
      renderer.dispose();
    };
  }, [vehicleType, vehicleColor, showHeadlights]);

  return (
    <div
      ref={containerRef}
      className={`relative w-full h-44 overflow-hidden rounded-xl bg-gradient-to-b from-neutral-900/60 to-black/60 border border-neutral-800/80 flex items-center justify-center cursor-grab active:cursor-grabbing ${className}`}
    >
      <canvas ref={canvasRef} className="w-full h-full block" />
      <div className="absolute bottom-2 right-2 text-[10px] text-neutral-500 bg-neutral-950/70 px-2 py-0.5 rounded pointer-events-none border border-neutral-800">
        Vue 3D • Glisser pour tourner
      </div>
    </div>
  );
};
