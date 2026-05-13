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
    
    private Maison maisonActuelle; // On travaille sur une maison pour l'instant
    private Piece pieceActuelle;   // La pièce en cours de construction
    private int compteurMur = 1;   // Pour donner un ID aux murs

    private Canvas zoneDessin;
    private TextArea zoneTexte;

    @Override
    public void start(Stage primaryStage) {
        // --- 1. ZONE DE DESSIN ---
        zoneDessin = new Canvas(600, 600);
        Pane conteneurDessin = new Pane(zoneDessin);
        conteneurDessin.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        dessinerGrilleVierge(); // Dessine la grille de fond

        // --- 2. MENU DROIT ---
        VBox menuDroite = new VBox(15);
        menuDroite.setPadding(new Insets(15));
        menuDroite.setPrefWidth(250);

        Label titre = new Label("Tableau de Bord");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnNouveau = new Button("1. Nouveau Projet");
        btnNouveau.setMaxWidth(Double.MAX_VALUE);

        Button btnAjouterMur = new Button("2. Ajouter un Mur");
        btnAjouterMur.setMaxWidth(Double.MAX_VALUE);
        btnAjouterMur.setDisable(true); // Désactivé tant qu'il n'y a pas de projet

        Button btnCalculer = new Button("3. Calculer le Devis");
        btnCalculer.setMaxWidth(Double.MAX_VALUE);
        btnCalculer.setDisable(true);

        zoneTexte = new TextArea();
        zoneTexte.setEditable(false);
        zoneTexte.setWrapText(true);
        zoneTexte.setPrefHeight(300);
        zoneTexte.setText("Bienvenue. Cliquez sur 'Nouveau Projet' pour commencer.");

        menuDroite.getChildren().addAll(titre, btnNouveau, btnAjouterMur, btnCalculer, zoneTexte);

        // --- 3. ACTIONS DES BOUTONS ---
        btnNouveau.setOnAction(e -> {
            maisonActuelle = new Maison("Mon Projet", 1);
            pieceActuelle = new Piece(1, 50); // Pièce pouvant contenir 50 murs
            maisonActuelle.ajouterPiece(pieceActuelle);
            compteurMur = 1;
            
            dessinerGrilleVierge();
            zoneTexte.setText("Nouveau projet créé ! Vous pouvez maintenant ajouter des murs.");
            btnAjouterMur.setDisable(false);
            btnCalculer.setDisable(false);
        });

        btnAjouterMur.setOnAction(e -> ouvrirFormulaireMur());

        btnCalculer.setOnAction(e -> {
            double total = maisonActuelle.devisBatiment();
            zoneTexte.setText("--- RÉSULTAT DU DEVIS ---\n\n");
            zoneTexte.appendText("Murs dessinés : " + (compteurMur - 1) + "\n");
            zoneTexte.appendText("Surface totale : " + maisonActuelle.surfaceSolBatiment() + " m2\n");
            zoneTexte.appendText("Montant total : " + total + " €");
        });

        // --- 4. ASSEMBLAGE ---
        BorderPane layoutPrincipal = new BorderPane();
        layoutPrincipal.setCenter(conteneurDessin);
        layoutPrincipal.setRight(menuDroite);

        Scene scene = new Scene(layoutPrincipal, 850, 600);
        primaryStage.setTitle("Projet Dev' Bâtiment - Saisie Interactive");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- LOGIQUE DE SAISIE (LE POP-UP) ---
    private void ouvrirFormulaireMur() {
        // Création de la boîte de dialogue
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Mur");
        dialog.setHeaderText("Saisissez les coordonnées du mur (en mètres)");

        // Création des champs de texte
        TextField txtX1 = new TextField(); txtX1.setPromptText("Ex: 1.0");
        TextField txtY1 = new TextField(); txtY1.setPromptText("Ex: 1.0");
        TextField txtX2 = new TextField(); txtX2.setPromptText("Ex: 5.0");
        TextField txtY2 = new TextField(); txtY2.setPromptText("Ex: 1.0");

        // Organisation dans une grille
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Début X1 :"), 0, 0); grid.add(txtX1, 1, 0);
        grid.add(new Label("Début Y1 :"), 0, 1); grid.add(txtY1, 1, 1);
        grid.add(new Label("Fin X2 :"), 0, 2);   grid.add(txtX2, 1, 2);
        grid.add(new Label("Fin Y2 :"), 0, 3);   grid.add(txtY2, 1, 3);
        dialog.getDialogPane().setContent(grid);

        // Ajout des boutons Valider et Annuler
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Ce qu'il se passe quand on clique sur "OK"
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // On convertit le texte tapé en nombres décimaux
                    double x1 = Double.parseDouble(txtX1.getText());
                    double y1 = Double.parseDouble(txtY1.getText());
                    double x2 = Double.parseDouble(txtX2.getText());
                    double y2 = Double.parseDouble(txtY2.getText());

                    // On crée le mur dans notre logique métier
                    Mur nouveauMur = new Mur(compteurMur, new Coin(1, x1, y1), new Coin(2, x2, y2), 10);
                    pieceActuelle.ajouterMur(nouveauMur);
                    compteurMur++;

                    // On met à jour l'écran
                    actualiserDessin();
                    zoneTexte.setText("Mur ajouté avec succès ! Longueur : " + String.format("%.2f", nouveauMur.longueur()) + " m");

                } catch (NumberFormatException ex) {
                    zoneTexte.setText("Erreur : Veuillez taper uniquement des chiffres avec un point (ex: 4.5).");
                }
            }
        });
    }

    // --- LOGIQUE DE DESSIN ---
    private void dessinerGrilleVierge() {
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        gc.clearRect(0, 0, zoneDessin.getWidth(), zoneDessin.getHeight());
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (int i = 0; i < 600; i += 50) {
            gc.strokeLine(i, 0, i, 600);
            gc.strokeLine(0, i, 600, i);
        }
    }

    private void actualiserDessin() {
        dessinerGrilleVierge();
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(4);

        // On redessine tous les murs de la pièce actuelle
        for (Mur mur : pieceActuelle.getMurs()) {
            if (mur != null) {
                double x1 = mur.getDebut().getCx() * 50;
                double y1 = mur.getDebut().getCy() * 50;
                double x2 = mur.getFin().getCx() * 50;
                double y2 = mur.getFin().getCy() * 50;
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}