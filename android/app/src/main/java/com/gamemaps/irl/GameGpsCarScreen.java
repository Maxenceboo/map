package com.gamemaps.irl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.AppManager;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.SurfaceCallback;
import androidx.car.app.SurfaceContainer;
import androidx.car.app.model.Action;
import androidx.car.app.model.ActionStrip;
import androidx.car.app.model.CarColor;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.Distance;
import androidx.car.app.model.Template;
import androidx.car.app.navigation.model.Maneuver;
import androidx.car.app.navigation.model.NavigationTemplate;
import androidx.car.app.navigation.model.RoutingInfo;
import androidx.car.app.navigation.model.Step;

/**
 * Écran principal affiché sur le tableau de bord de la voiture dans Android Auto.
 */
public class GameGpsCarScreen extends Screen implements SurfaceCallback {

    private String currentTheme = "GTA V";
    private Surface carSurface = null;
    private Rect surfaceVisibleArea = null;

    public GameGpsCarScreen(@NonNull CarContext carContext) {
        super(carContext);
        // Enregistre le callback pour recevoir la surface graphique de l'écran de la voiture
        getCarContext().getCarService(AppManager.class).setSurfaceCallback(this);
    }

    @Override
    public void onSurfaceAvailable(@NonNull SurfaceContainer surfaceContainer) {
        this.carSurface = surfaceContainer.getSurface();
        renderMapOnCarSurface();
    }

    @Override
    public void onVisibleAreaChanged(@NonNull Rect visibleArea) {
        this.surfaceVisibleArea = visibleArea;
        renderMapOnCarSurface();
    }

    @Override
    public void onStableAreaChanged(@NonNull Rect stableArea) {
    }

    @Override
    public void onSurfaceDestroyed(@NonNull SurfaceContainer surfaceContainer) {
        this.carSurface = null;
    }

    /**
     * Rendu visuel direct sur la surface de l'écran Android Auto de la voiture
     */
    private void renderMapOnCarSurface() {
        if (carSurface == null || !carSurface.isValid()) return;

        try {
            Canvas canvas = carSurface.lockCanvas(null);
            if (canvas != null) {
                // Fond selon le thème actif
                int bgColor = currentTheme.equals("Minecraft") ? Color.rgb(46, 125, 50) : Color.rgb(15, 17, 21);
                canvas.drawColor(bgColor);

                Paint paint = new Paint();
                paint.setAntiAlias(true);

                // Dessin du quadrillage / style jeu
                paint.setColor(Color.argb(40, 255, 255, 255));
                paint.setStrokeWidth(2f);
                for (int x = 0; x < canvas.getWidth(); x += 80) {
                    canvas.drawLine(x, 0, x, canvas.getHeight(), paint);
                }
                for (int y = 0; y < canvas.getHeight(); y += 80) {
                    canvas.drawLine(0, y, canvas.getWidth(), y, paint);
                }

                // Affichage du logo du thème au centre
                paint.setColor(Color.WHITE);
                paint.setTextSize(48f);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("GAME MAPS IRL (" + currentTheme + ")", canvas.getWidth() / 2f, canvas.getHeight() / 2f - 40, paint);

                paint.setTextSize(26f);
                paint.setColor(Color.rgb(180, 180, 190));
                canvas.drawText("GPS Voiture connecté — Prêt pour la conduite", canvas.getWidth() / 2f, canvas.getHeight() / 2f + 20, paint);

                carSurface.unlockCanvasAndPost(canvas);
            }
        } catch (Exception e) {
            // Ignorer si la surface change
        }
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        // 1. Boutons d'action pour la voiture (Changement de thème & Contrôles)
        ActionStrip actionStrip = new ActionStrip.Builder()
                .addAction(new Action.Builder()
                        .setTitle("GTA V")
                        .setOnClickListener(() -> {
                            currentTheme = "GTA V";
                            renderMapOnCarSurface();
                            invalidate();
                        })
                        .build())
                .addAction(new Action.Builder()
                        .setTitle("Minecraft")
                        .setOnClickListener(() -> {
                            currentTheme = "Minecraft";
                            renderMapOnCarSurface();
                            invalidate();
                        })
                        .build())
                .addAction(new Action.Builder()
                        .setTitle("Waze")
                        .setOnClickListener(() -> {
                            currentTheme = "Waze";
                            renderMapOnCarSurface();
                            invalidate();
                        })
                        .build())
                .build();

        // 2. Étape de navigation avec virage
        Maneuver maneuver = new Maneuver.Builder(Maneuver.TYPE_STRAIGHT).build();
        Step step = new Step.Builder("Continuez sur la route principale")
                .setManeuver(maneuver)
                .build();

        Distance dist = Distance.create(500, Distance.UNIT_METERS);
        RoutingInfo routingInfo = new RoutingInfo.Builder()
                .setCurrentStep(step, dist)
                .build();

        // 3. Gabarit de navigation officiel d'Android Auto
        return new NavigationTemplate.Builder()
                .setNavigationInfo(routingInfo)
                .setActionStrip(actionStrip)
                .setBackgroundColor(CarColor.createCustom(0xFF0d0e12, 0xFF0d0e12))
                .build();
    }
}
