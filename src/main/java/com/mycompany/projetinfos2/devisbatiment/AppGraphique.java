package com.mycompany.projetinfos2.devisbatiment;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AppGraphique extends Application {
    
    private Batiment batimentActuel; // Le bâtiment en mémoire
    private Canvas zoneDessin;       // La toile pour dessiner
    private TextArea zoneTexte;      // L'écran de la console intégré

    @Override
    public void start(Stage primaryStage) {
        // --- 1. ZONE DE DESSIN (Au centre) ---
        zoneDessin = new Canvas(600, 600);
        Pane conteneurDessin = new Pane(zoneDessin);
        conteneurDessin.setStyle("-fx-background-color: white; -fx-border-color: gray;");

        // --- 2. MENU DROIT (Boutons) ---
        VBox menuDroite = new VBox(15); // 15px d'espace entre les éléments
        menuDroite.setPadding(new Insets(15));
        menuDroite.setPrefWidth(250);

        Label titre = new Label("Tableau de Bord");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnGenererMaison = new Button("Générer Maison Test");
        btnGenererMaison.setMaxWidth(Double.MAX_VALUE);

        Button btnCalculerDevis = new Button("Calculer le Devis");
        btnCalculerDevis.setMaxWidth(Double.MAX_VALUE);

        zoneTexte = new TextArea();
        zoneTexte.setEditable(false);
        zoneTexte.setWrapText(true);
        zoneTexte.setPrefHeight(300);

        menuDroite.getChildren().addAll(titre, btnGenererMaison, btnCalculerDevis, zoneTexte);

        // --- 3. QUE FONT LES BOUTONS ? (Les actions) ---
        btnGenererMaison.setOnAction(e -> {
            creerMaisonTest(); // Crée les données
            dessinerPlan();    // Dessine le plan
            zoneTexte.setText("Une maison de test de 4x4m a été générée !\nLes murs bleus s'affichent sur le plan.");
        });

        btnCalculerDevis.setOnAction(e -> {
            if (batimentActuel != null) {
                double total = batimentActuel.devisBatiment();
                zoneTexte.setText("--- RÉSULTAT DU DEVIS ---\n\n");
                zoneTexte.appendText("Surface totale : " + batimentActuel.surfaceSolBatiment() + " m2\n");
                zoneTexte.appendText("Montant total : " + total + " €");
            } else {
                zoneTexte.setText("Erreur : Veuillez générer un bâtiment avant de calculer le devis.");
            }
        });

        // --- 4. ASSEMBLAGE DE LA FENÊTRE ---
        BorderPane layoutPrincipal = new BorderPane();
        layoutPrincipal.setCenter(conteneurDessin);
        layoutPrincipal.setRight(menuDroite);

        Scene scene = new Scene(layoutPrincipal, 850, 600);
        primaryStage.setTitle("Projet Dev' Bâtiment - Architecte 2D");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- LOGIQUE MÉTIER ---
    
    private void creerMaisonTest() {
        // On simule ce que l'utilisateur aurait tapé dans la console
        Maison m = new Maison("Maison Temoin", 1);
        Piece p = new Piece(1, 4);
        
        // On crée une pièce carrée de 4 mètres sur 4, qui commence aux coordonnées (1,1)
        p.ajouterMur(new Mur(1, new Coin(1, 1.0, 1.0), new Coin(2, 5.0, 1.0), 2)); // Haut
        p.ajouterMur(new Mur(2, new Coin(2, 5.0, 1.0), new Coin(3, 5.0, 5.0), 2)); // Droite
        p.ajouterMur(new Mur(3, new Coin(3, 5.0, 5.0), new Coin(4, 1.0, 5.0), 2)); // Bas
        p.ajouterMur(new Mur(4, new Coin(4, 1.0, 5.0), new Coin(1, 1.0, 1.0), 2)); // Gauche
        
        m.ajouterPiece(p);
        batimentActuel = m; // On sauvegarde dans la mémoire
    }

    private void dessinerPlan() {
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        gc.clearRect(0, 0, zoneDessin.getWidth(), zoneDessin.getHeight()); // Effacer l'écran

        // Dessiner une grille (1 carreau = 1 mètre)
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (int i = 0; i < 600; i += 50) {
            gc.strokeLine(i, 0, i, 600);
            gc.strokeLine(0, i, 600, i);
        }

        // Dessiner la maison
        if (batimentActuel instanceof Maison) {
            Maison m = (Maison) batimentActuel;
            gc.setStroke(Color.DARKBLUE); // Les murs seront bleus
            gc.setLineWidth(4);

            for (Piece p : m.getPieces()) {
                if (p == null) continue;
                for (Mur mur : p.getMurs()) {
                    if (mur == null) continue;
                    
                    // L'ECHELLE EST CRUCIALE : 1 mètre mathématique = 50 pixels sur l'écran
                    double x1 = mur.getDebut().getCx() * 50;
                    double y1 = mur.getDebut().getCy() * 50;
                    double x2 = mur.getFin().getCx() * 50;
                    double y2 = mur.getFin().getCy() * 50;

                    gc.strokeLine(x1, y1, x2, y2); // On trace la ligne !
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}