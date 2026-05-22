/* =========================================================================
 * NOM DE LA CLASSE : AppGraphique
 * DATE DE MISE À JOUR : 22 Mai 2026
 * CATÉGORIE TECHNIQUE : Interface Graphique (JavaFX - Fenêtre principale)
 * =========================================================================
 * DESCRIPTION :
 * Classe principale pour l'interface graphique JavaFX de l'application.
 * Elle permet de dessiner le bâtiment visuellement, choisir les revêtements, de générer le devis puis l'exporter.
 * =========================================================================
 * @author Siméon, Clémentine
 * =========================================================================
 */

package com.mycompany.projetinfos2.devisbatiment;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class AppGraphique extends Application {
    
    // --- DONNÉES ET ÉTAT DU PROJET ---
    private Batiment projetActuel;     
    private Piece pieceActuelle;       
    private Niveau niveauActuel;       
    private Niveau niveauAffiche;      
    private Appartement appartActuel;  
    
    // --- COMPTEURS AUTOMATIQUES ---
    private int compteurMur = 1;
    private int compteurPiece = 1;
    private int compteurNiveau = 0;
    private int compteurAppart = 0;
    
    // --- VARIABLES POUR LE DESSIN ET LA SOURIS ---
    private boolean pieceEnCours = false; 
    private TextField champTx1, champTy1, champTx2, champTy2; 
    private boolean saisieMurEnCours = false; 
    private boolean attendPremierClic = false; 
    private boolean pointArriveeVerrouille = false; 
    
    // --- PARAMÈTRES D'AFFICHAGE (ZOOM ET DÉPLACEMENT) ---
    private double zoom = 1.0;          
    private double panX = 100;          
    private double panY = 300;          
    private double lastMouseX, lastMouseY; 
    
    // --- COMPOSANTS VISUELS ---
    private ArrayList<Revetement> catalogue = new ArrayList<>(); 
    private Canvas zoneDessin;          
    private TextArea zoneTexte;         
    private TreeView<String> arbreProjet; 
    
    // --- BOUTONS DE L'INTERFACE ---
    private Button btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne;

    @Override
    public void start(Stage primaryStage) {
        // 1. Initialisation prioritaire de la zone de texte
        zoneTexte = new TextArea("Bienvenue ! Créez un projet pour commencer.");
        zoneTexte.setEditable(false); 
        zoneTexte.setPrefHeight(120);
        zoneTexte.setWrapText(true); 

        // 2. Chargement sécurisé des données
        chargerCatalogue("CatalogueRevetements.txt"); 
        
        primaryStage.setTitle("Calculateur de Devis Bâtiment - Version Graphique");
        
        // --- CRÉATION DE LA ZONE DE DESSIN ---
        zoneDessin = new Canvas(800, 600);
        actualiserDessin(); 
        
        // --- INTERACTION : SUIVI DE LA SOURIS POUR LES COORDONNÉES EN DIRECT ---
        zoneDessin.setOnMouseMoved(e -> {
            if (saisieMurEnCours) {
                double mx = Math.round(((e.getX() - panX) / (50 * zoom)) * 100.0) / 100.0;
                double my = Math.round(((e.getY() - panY) / (50 * zoom)) * 100.0) / 100.0;

                if (attendPremierClic) {
                    champTx1.setText(String.valueOf(mx));
                    champTy1.setText(String.valueOf(my));
                } else if (!pointArriveeVerrouille) {
                    if (pieceActuelle != null && pieceActuelle.getMurs()[0] != null) {
                        Coin origine = pieceActuelle.getMurs()[0].getDebut();
                        double dist = Math.sqrt(Math.pow(mx - origine.getCx(), 2) + Math.pow(my - origine.getCy(), 2));
                        if (dist < 0.4) {
                            mx = origine.getCx();
                            my = origine.getCy();
                        }
                    }
                    champTx2.setText(String.valueOf(mx));
                    champTy2.setText(String.valueOf(my));
                }
            }
        });

        // --- INTERACTION : CLIC SOURIS SUR LE PLAN POUR VERROUILLER ---
        zoneDessin.setOnMouseClicked(e -> {
            if (saisieMurEnCours) {
                if (attendPremierClic) {
                    attendPremierClic = false;
                    pointArriveeVerrouille = false;
                    zoneTexte.setText("Départ fixé ! Bougez la souris pour l'arrivée et cliquez pour verrouiller.");
                } else {
                    pointArriveeVerrouille = !pointArriveeVerrouille;
                    if (pointArriveeVerrouille) {
                        double mx = Double.parseDouble(champTx2.getText());
                        double my = Double.parseDouble(champTy2.getText());
                        if (pieceActuelle.getMurs()[0] != null) {
                            Coin origine = pieceActuelle.getMurs()[0].getDebut();
                            if (mx == origine.getCx() && my == origine.getCy()) {
                                zoneTexte.setText("Fermeture détectée et verrouillée ! Validez le formulaire.");
                            } else {
                                zoneTexte.setText("Arrivée verrouillée. Validez ou recliquez sur le plan pour modifier.");
                            }
                        } else {
                            zoneTexte.setText("Arrivée verrouillée. Validez ou recliquez sur le plan pour modifier.");
                        }
                    } else {
                        zoneTexte.setText("Arrivée déverrouillée. Bougez la souris...");
                    }
                }
            }
        });

        // --- GESTION DU DÉPLACEMENT DU PLAN ET DU ZOOM ---
        zoneDessin.setOnMousePressed(e -> { lastMouseX = e.getX(); lastMouseY = e.getY(); });
        zoneDessin.setOnMouseDragged(e -> {
            if (!saisieMurEnCours) { 
                panX += (e.getX() - lastMouseX); panY += (e.getY() - lastMouseY);
                lastMouseX = e.getX(); lastMouseY = e.getY();
                actualiserDessin();
            }
        });
        zoneDessin.setOnScroll(e -> {
            if (e.getDeltaY() > 0) zoom *= 1.1; 
            else zoom /= 1.1;                   
            zoom = Math.max(0.2, Math.min(zoom, 5.0)); 
            actualiserDessin();
        });

        // --- INTERACTION : SÉLECTION DANS L'ARBRE (EXPLORATEUR DE GAUCHE) ---
        arbreProjet = new TreeView<>();
        arbreProjet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getValue().startsWith("Niveau ")) {
                try {
                    int idN = Integer.parseInt(newValue.getValue().substring(7));
                    if (projetActuel instanceof Immeuble) {
                        for (Niveau n : ((Immeuble) projetActuel).getNiveaux()) {
                            if (n != null && n.getId() == idN) {
                                niveauAffiche = n;
                                actualiserDessin();
                                break;
                            }
                        }
                    }
                } catch(Exception ex) {}
            }
        });

        // --- INITIALISATION DES BOUTONS DE L'INTERFACE ---
        btnNouveau = new Button("🏗️ Nouveau Projet"); btnAjoutNiveau = new Button("➕ Ajouter un Niveau");
        btnAjoutAppart = new Button("🚪 Ajouter un Appartement"); btnNouvellePiece = new Button("📐 Nouvelle Pièce");
        btnAjouterMur = new Button("🧱 Ajouter un Mur"); btnTerminerPiece = new Button("🛑 Forcer Fin Pièce");
        btnCalculer = new Button("🧮 Calculer le Devis"); btnVoirCatalogue = new Button("📖 Voir le Catalogue");
        btnExporter = new Button("💾 Exporter le Devis"); btnConsigne = new Button("💡 Consignes d'utilisation");
        
        Button[] tousLesBoutons = {btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne};
        for(Button b : tousLesBoutons) b.setMaxWidth(Double.MAX_VALUE);

        // --- CONFIGURATION DES ACTIONS DES BOUTONS ---
        btnNouveau.setOnAction(e -> initProjet());
        btnAjoutNiveau.setOnAction(e -> {
            if (projetActuel instanceof Immeuble) {
                compteurAppart = 0; 
                Niveau n = new Niveau(++compteurNiveau, 2.50, 10);
                ((Immeuble) projetActuel).ajouterNiveau(n);
                niveauActuel = n; niveauAffiche = n; appartActuel = null;
                zoneTexte.setText("Niveau " + compteurNiveau + " ajouté ! Ajoutez un appartement.");
                actualiserArbre(); majBoutons();
            }
        });
        btnAjoutAppart.setOnAction(e -> {
            if (niveauActuel != null) {
                Appartement a = new Appartement(++compteurAppart, 20);
                niveauActuel.ajouterAppart(a);
                appartActuel = a;
                zoneTexte.setText("Appartement " + compteurAppart + " créé ! Créez une pièce maintenant.");
                actualiserArbre(); majBoutons();
            }
        });
        btnNouvellePiece.setOnAction(e -> {
            pieceActuelle = new Piece(compteurPiece++, 50); 
            pieceEnCours = true; compteurMur = 1;
            zoneTexte.setText("Pièce initialisée. Cliquez sur 'Ajouter un mur' pour dessiner.");
            majBoutons();
        });
        btnAjouterMur.setOnAction(e -> ouvrirFormulaireMur());
        btnTerminerPiece.setOnAction(e -> finaliserPiece());
        
        btnCalculer.setOnAction(e -> {
            double totalHT = projetActuel.devisBatiment();
            double tva = totalHT * 0.20; 
            double totalTTC = totalHT + tva;
            zoneTexte.setText("=== RÉCAPITULATIF DU DEVIS ===\nMontant HT  : " + String.format("%.2f", totalHT) + " €\nTVA (20%)   : " + String.format("%.2f", tva) + " €\nMontant TTC : " + String.format("%.2f", totalTTC) + " €");
        });
        
        btnVoirCatalogue.setOnAction(e -> {
            Dialog<Void> d = new Dialog<>(); d.setTitle("Catalogue des Revêtements");
            TextArea t = new TextArea(); t.setEditable(false);
            for(Revetement r : catalogue) t.appendText("ID " + r.getId() + " - " + r.getDesignation() + " (" + r.getPrix() + " €/m2)\n");
            d.getDialogPane().setContent(t); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            d.showAndWait();
        });
        
        btnConsigne.setOnAction(e -> afficherConsignes());
        btnExporter.setOnAction(e -> exporterDevis());

        // --- MISE EN PAGE GLOBALE (LAYOUT STRUCTURÉ) ---
        
        // NOUVEAU : ENCADRÉ LÉGENDE VISUELLE POUR LE PLAN
        VBox encadreLegende = new VBox(5);
        encadreLegende.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 10; -fx-border-radius: 5;");
        Label titreL = new Label("Légende du Plan :"); titreL.setStyle("-fx-font-weight: bold;");
        Label lMur = new Label("▬ Murs (Trait bleu/rouge)"); lMur.setTextFill(Color.DARKBLUE);
        Label lPorte = new Label("■ Porte (Carré vert)"); lPorte.setTextFill(Color.GREEN);
        Label lFenetre = new Label("● Fenêtre (Rond rouge)"); lFenetre.setTextFill(Color.RED);
        encadreLegende.getChildren().addAll(titreL, lMur, lPorte, lFenetre);

        // PANNEAU LATÉRAL GAUCHE (Légende et arbre de projet)
        VBox menuGauche = new VBox(10);
        menuGauche.setPadding(new Insets(10)); 
        menuGauche.setPrefWidth(220);
        Label lblExplo = new Label("Explorateur :"); lblExplo.setStyle("-fx-font-weight: bold;");
        VBox.setVgrow(arbreProjet, Priority.ALWAYS); 
        menuGauche.getChildren().addAll(encadreLegende, lblExplo, arbreProjet);

        // PANNEAU LATÉRAL DROIT (Boutons et notifications)
        VBox menuDroite = new VBox(10);
        menuDroite.setPadding(new Insets(10)); 
        menuDroite.setPrefWidth(220);
        menuDroite.getChildren().addAll(
            new Label("Actions :"), btnNouveau, btnAjoutNiveau, btnAjoutAppart, 
            btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, 
            btnVoirCatalogue, btnExporter, btnConsigne, 
            new Label("Informations :"), zoneTexte
        );

        // PANNEAU CENTRAL BORDERPANE
        BorderPane principal = new BorderPane();
        principal.setLeft(menuGauche);   
        principal.setCenter(zoneDessin); 
        principal.setRight(menuDroite);  

        majBoutons(); 

        primaryStage.setScene(new Scene(principal, 1250, 650));
        primaryStage.show();
    }

    private void initProjet() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paramètres du Nouveau Projet");
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        TextField txtNom = new TextField("MonProjet");
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Maison (Plain-pied)", "Immeuble / Étage");
        cbType.getSelectionModel().selectFirst();
        
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Nom du projet :"), 0, 0); grid.add(txtNom, 1, 0);
        grid.add(new Label("Type de bâtiment :"), 0, 1); grid.add(cbType, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                compteurPiece = 1; compteurNiveau = 0; compteurAppart = 0;
                pieceEnCours = false; pieceActuelle = null; appartActuel = null;
                
                if (cbType.getSelectionModel().getSelectedIndex() == 0) {
                    projetActuel = new Maison(txtNom.getText(), 100);
                    niveauActuel = null; niveauAffiche = null;
                    zoneTexte.setText("Projet Maison créé. Ajoutez directement une pièce !");
                } else {
                    projetActuel = new Immeuble(txtNom.getText(), 50);
                    Niveau n0 = new Niveau(0, 2.50, 10); 
                    ((Immeuble) projetActuel).ajouterNiveau(n0);
                    niveauActuel = n0; niveauAffiche = n0;
                    zoneTexte.setText("Projet Immeuble créé. RDC configuré (Niveau 0). Ajoutez un appartement.");
                }
                actualiserDessin(); actualiserArbre(); majBoutons();
            }
        });
    }

    private void ouvrirFormulaireMur() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Mur");
        dialog.initModality(javafx.stage.Modality.NONE);
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        Mur dernierMur = null;
        for (Mur m : pieceActuelle.getMurs()) if (m != null) dernierMur = m;
        TextField tx1 = new TextField(), ty1 = new TextField(), tx2 = new TextField(), ty2 = new TextField();
        
        if (dernierMur != null) {
            tx1.setText(String.valueOf(dernierMur.getFin().getCx())); 
            ty1.setText(String.valueOf(dernierMur.getFin().getCy()));
            tx1.setEditable(false); ty1.setEditable(false);
            tx1.setStyle("-fx-background-color: #eeeeee;"); ty1.setStyle("-fx-background-color: #eeeeee;");
            attendPremierClic = false; 
            zoneTexte.setText("Bougez la souris pour placer l'arrivée du mur, puis cliquez pour verrouiller.");
        } else {
            attendPremierClic = true; 
            zoneTexte.setText("1er mur : Bougez la souris et cliquez pour fixer le DÉPART.");
        }

        champTx1 = tx1; champTy1 = ty1; champTx2 = tx2; champTy2 = ty2;
        saisieMurEnCours = true; pointArriveeVerrouille = false;
        btnAjouterMur.setDisable(true); 

        TextField tp = new TextField("0"), tf = new TextField("0");
        ComboBox<String> cb = new ComboBox<>(); cb.getItems().add("Aucun");
        for (Revetement r : catalogue) if (r.estPourMur()) cb.getItems().add(r.getDesignation());
        cb.getSelectionModel().selectFirst();
        
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Début X, Y (m):"), 0, 0); g.add(tx1, 1, 0); g.add(ty1, 2, 0);
        g.add(new Label("Fin X, Y (m):"), 0, 1); g.add(tx2, 1, 1); g.add(ty2, 2, 1);
        g.add(new Label("Nombre de Portes / Fenêtres :"), 0, 2); g.add(tp, 1, 2); g.add(tf, 2, 2);
        g.add(new Label("Revêtement mural :"), 0, 3); g.add(cb, 1, 3, 2, 1);
        dialog.getDialogPane().setContent(g); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setDisable(true);
        
        Runnable verif = () -> btnOk.setDisable(tx2.getText().isEmpty() || ty2.getText().isEmpty() || tx1.getText().isEmpty() || ty1.getText().isEmpty());
        tx1.textProperty().addListener((o, old, n) -> verif.run()); ty1.textProperty().addListener((o, old, n) -> verif.run());
        tx2.textProperty().addListener((o, old, n) -> verif.run()); ty2.textProperty().addListener((o, old, n) -> verif.run());

        dialog.showAndWait().ifPresent(r -> {
            saisieMurEnCours = false; 
            if (r == ButtonType.OK) {
                try {
                    Mur m = new Mur(compteurMur++, new Coin(1, Double.parseDouble(tx1.getText()), Double.parseDouble(ty1.getText())), 
                                   new Coin(2, Double.parseDouble(tx2.getText()), Double.parseDouble(ty2.getText())), 20);
                    
                    for (int i=0; i<Integer.parseInt(tp.getText()); i++) m.ajouterOuverture(new Porte(i));
                    for (int i=0; i<Integer.parseInt(tf.getText()); i++) m.ajouterOuverture(new Fenetre(i));
                    
                    if (!cb.getValue().equals("Aucun")) {
                        for (Revetement rev : catalogue) if (cb.getValue().equals(rev.getDesignation())) m.appliquerRevetement(rev);
                    }
                    
                    pieceActuelle.ajouterMur(m);
                    
                    Mur pre = pieceActuelle.getMurs()[0];
                    if (compteurMur > 3 && m.getFin().getCx() == pre.getDebut().getCx() && m.getFin().getCy() == pre.getDebut().getCy()) {
                        actualiserDessin(); actualiserArbre(); finaliserPiece();
                    } else {
                        actualiserDessin(); actualiserArbre();
                    }
                } catch (Exception ex) { zoneTexte.setText("Erreur dans les formats numériques."); }
            }
            majBoutons();
        });
        saisieMurEnCours = false; 
        majBoutons();
    }

    private void finaliserPiece() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fin de la pièce - Choix des surfaces horizontales");
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        TextField txtNomPiece = new TextField("Pièce n°" + (compteurPiece-1));
        ComboBox<String> cbSol = new ComboBox<>(), cbPlat = new ComboBox<>();
        cbSol.getItems().add("Aucun"); cbPlat.getItems().add("Aucun");
        
        for(Revetement r : catalogue) {
            if(r.estPourSol()) cbSol.getItems().add(r.getDesignation());
            if(r.estPourPlafond()) cbPlat.getItems().add(r.getDesignation());
        }
        cbSol.getSelectionModel().selectFirst(); cbPlat.getSelectionModel().selectFirst();
        
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Nommer la pièce :"), 0, 0); g.add(txtNomPiece, 1, 0);
        g.add(new Label("Revêtement Sol :"), 0, 1); g.add(cbSol, 1, 1);
        g.add(new Label("Revêtement Plafond :"), 0, 2); g.add(cbPlat, 1, 2);
        dialog.getDialogPane().setContent(g); dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        
        dialog.showAndWait().ifPresent(r -> {
            for(Revetement rev : catalogue) {
                if(cbSol.getValue().equals(rev.getDesignation())) pieceActuelle.getSol().appliquerRevetement(rev);
                if(cbPlat.getValue().equals(rev.getDesignation())) pieceActuelle.getPlafond().appliquerRevetement(rev);
            }
            
            if (projetActuel instanceof Maison) {
                ((Maison) projetActuel).ajouterPiece(pieceActuelle);
            } else if (appartActuel != null) {
                appartActuel.ajouterPiece(pieceActuelle);
            }
            
            pieceEnCours = false; pieceActuelle = null;
            zoneTexte.setText("Pièce enregistrée avec succès !");
            actualiserDessin(); actualiserArbre(); majBoutons();
        });
    }

    private void actualiserDessin() {
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        gc.clearRect(0, 0, zoneDessin.getWidth(), zoneDessin.getHeight());
        
        // --- GRILLE ---
        gc.setStroke(Color.web("#e0e0e0")); gc.setLineWidth(0.5);
        double tailleCase = 50 * zoom;
        for (double x = panX % tailleCase; x < zoneDessin.getWidth(); x += tailleCase) gc.strokeLine(x, 0, x, zoneDessin.getHeight());
        for (double y = panY % tailleCase; y < zoneDessin.getHeight(); y += tailleCase) gc.strokeLine(0, y, zoneDessin.getWidth(), y);
        
        gc.setStroke(Color.web("#b0b0b0")); gc.setLineWidth(1.5);
        gc.strokeLine(panX, 0, panX, zoneDessin.getHeight());
        gc.strokeLine(0, panY, zoneDessin.getWidth(), panY);
        
        // --- DESSIN DES PIÈCES ---
        if (projetActuel == null) return;
        gc.setLineWidth(3.0);
        
        if (projetActuel instanceof Maison) {
            gc.setStroke(Color.DARKBLUE);
            for (Piece p : ((Maison) projetActuel).getPieces()) if (p != null) dessinerMursPiece(gc, p);
        } else if (niveauAffiche != null) {
            gc.setStroke(Color.DARKRED);
            for (Appartement app : niveauAffiche.getApparts()) {
                if (app != null) {
                    for (Piece p : app.getPieces()) if (p != null) dessinerMursPiece(gc, p);
                }
            }
        }
        
        if (pieceEnCours && pieceActuelle != null) {
            gc.setStroke(Color.DEEPSKYBLUE); gc.setLineWidth(4.0);
            dessinerMursPiece(gc, pieceActuelle);
        }
    }

    /**
     * NOUVEAU : Dessine les murs, les portes, les fenêtres, et écrit le nom de la pièce au centre.
     */
    private void dessinerMursPiece(GraphicsContext gc, Piece p) {
        double sommeX = 0, sommeY = 0;
        int nbPointsPourCentre = 0;

        for (Mur m : p.getMurs()) {
            if (m != null) {
                // 1. Calcul et tracé du mur principal
                double x1 = m.getDebut().getCx() * (50 * zoom) + panX;
                double y1 = m.getDebut().getCy() * (50 * zoom) + panY;
                double x2 = m.getFin().getCx() * (50 * zoom) + panX;
                double y2 = m.getFin().getCy() * (50 * zoom) + panY;
                
                gc.strokeLine(x1, y1, x2, y2);
                gc.setFill(Color.BLACK);
                gc.fillOval(x1-3, y1-3, 6, 6); // Points aux angles
                
                // Pour le centre de la pièce (moyenne des points de départ)
                sommeX += x1;
                sommeY += y1;
                nbPointsPourCentre++;

                // 2. Dessin des Ouvertures (Portes et Fenêtres) le long du mur
                int nbPortes = 0;
                int nbFenetres = 0;
                for (int i = 0; i < m.getNbO(); i++) {
                    if (m.getOuvertures()[i] instanceof Porte) nbPortes++;
                    if (m.getOuvertures()[i] instanceof Fenetre) nbFenetres++;
                }

                int totalOuvertures = nbPortes + nbFenetres;
                if (totalOuvertures > 0) {
                    // On divise le mur en segments égaux pour espacer les ouvertures
                    double espacementX = (x2 - x1) / (totalOuvertures + 1);
                    double espacementY = (y2 - y1) / (totalOuvertures + 1);
                    
                    int ouvertureCourante = 1;
                    
                    // Placement des portes (Carrés verts)
                    gc.setFill(Color.GREEN);
                    for(int i = 0; i < nbPortes; i++) {
                        double ox = x1 + espacementX * ouvertureCourante;
                        double oy = y1 + espacementY * ouvertureCourante;
                        gc.fillRect(ox - 5, oy - 5, 10, 10);
                        ouvertureCourante++;
                    }
                    
                    // Placement des fenêtres (Ronds rouge)
                    gc.setFill(Color.RED);
                    for(int i = 0; i < nbFenetres; i++) {
                        double ox = x1 + espacementX * ouvertureCourante;
                        double oy = y1 + espacementY * ouvertureCourante;
                        gc.fillOval(ox - 5, oy - 5, 10, 10);
                        ouvertureCourante++;
                    }
                    
                    // On remet la couleur du "pinceau" à sa couleur d'origine pour les prochains murs
                    if (projetActuel instanceof Maison) gc.setStroke(Color.DARKBLUE);
                    else gc.setStroke(Color.DARKRED);
                    if (pieceEnCours && p == pieceActuelle) gc.setStroke(Color.DEEPSKYBLUE);
                }
            }
        }

        // 3. Dessin du Nom de la Pièce au centre
        if (nbPointsPourCentre > 2 && p != pieceActuelle) { // Uniquement si la pièce est finie
            double centreX = sommeX / nbPointsPourCentre;
            double centreY = sommeY / nbPointsPourCentre;
            
            gc.setFill(Color.BLACK);
            // La police grandit ou rétrécit avec le zoom
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14 * zoom)); 
            gc.fillText("Pièce n°" + p.getId(), centreX - (25 * zoom), centreY);
        }
    }

    private void actualiserArbre() {
        if (projetActuel == null) { arbreProjet.setRoot(null); return; }
        
        TreeItem<String> racine = new TreeItem<>(projetActuel.getId().toUpperCase());
        racine.setExpanded(true);
        
        if (projetActuel instanceof Maison) {
            for (Piece p : ((Maison) projetActuel).getPieces()) {
                if (p != null) racine.getChildren().add(new TreeItem<>("Pièce n°" + p.getId()));
            }
        } else {
            for (Niveau n : ((Immeuble) projetActuel).getNiveaux()) {
                if (n == null) continue;
                TreeItem<String> itemN = new TreeItem<>("Niveau " + n.getId());
                for (Appartement app : n.getApparts()) {
                    if (app == null) continue;
                    TreeItem<String> itemA = new TreeItem<>("Appartement " + app.getId());
                    for (Piece p : app.getPieces()) {
                        if (p != null) itemA.getChildren().add(new TreeItem<>("Pièce n°" + p.getId()));
                    }
                    itemN.getChildren().add(itemA);
                }
                racine.getChildren().add(itemN);
            }
        }
        arbreProjet.setRoot(racine);
    }

    private void majBoutons() {
        boolean aProjet = (projetActuel != null);
        boolean estImmeuble = aProjet && (projetActuel instanceof Immeuble);
        
        btnAjoutNiveau.setDisable(!estImmeuble || pieceEnCours);
        btnAjoutAppart.setDisable(!estImmeuble || niveauActuel == null || pieceEnCours);
        
        if (estImmeuble) btnNouvellePiece.setDisable(appartActuel == null || pieceEnCours);
        else btnNouvellePiece.setDisable(!aProjet || pieceEnCours);
        
        btnAjouterMur.setDisable(!pieceEnCours || saisieMurEnCours);
        btnTerminerPiece.setDisable(!pieceEnCours || compteurMur < 3);
        btnCalculer.setDisable(!aProjet || pieceEnCours);
        btnVoirCatalogue.setDisable(pieceEnCours);
        btnExporter.setDisable(!aProjet || pieceEnCours);
        btnConsigne.setDisable(pieceEnCours);
    }

    private void chargerCatalogue(String f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); String l;
            while ((l = br.readLine()) != null) {
                String[] t = l.split(";");
                int id = Integer.parseInt(t[0]);
                double p = Double.parseDouble(t[5]);
                catalogue.add(new Peinture(id, t[1], p, t[2].equals("1"), t[3].equals("1"), t[4].equals("1")));
            }
        } catch (Exception e) { zoneTexte.setText("Attention: fichier catalogue non trouvé."); }
    }

    private void exporterDevis() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le Devis Détaillé");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichier Texte", "*.txt"));
        fileChooser.setInitialFileName("ResultatDevis_" + projetActuel.getId() + ".txt");
        
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                pw.println("===========================================================");
                pw.println("             DEVIS DÉTAILLÉ - " + projetActuel.getId().toUpperCase());
                pw.println("             Généré le : " + new java.util.Date());
                pw.println("===========================================================");
                
                double totalHT = projetActuel.devisBatiment();
                double tva = totalHT * 0.20;
                double totalTTC = totalHT + tva;

                pw.println("SURFACE SOL TOTALE : " + String.format("%.2f", projetActuel.surfaceSolBatiment()) + " m2");
                pw.println("MONTANT TOTAL HT   : " + String.format("%.2f", totalHT) + " €");
                pw.println("TVA (20%)          : " + String.format("%.2f", tva) + " €");
                pw.println("MONTANT TOTAL TTC  : " + String.format("%.2f", totalTTC) + " €");
                pw.println("===========================================================");
                
                if (projetActuel instanceof Maison) {
                    for (Piece p : ((Maison)projetActuel).getPieces()) if (p != null) ecrirePiece(pw, p, 2.5); 
                } else {
                    for (Niveau n : ((Immeuble)projetActuel).getNiveaux()) {
                        if (n == null) continue;
                        pw.println("\n>>> NIVEAU " + n.getId() + " [HSP : " + n.getHauteur() + "m]");
                        for (Appartement app : n.getApparts()) {
                            if (app == null) continue;
                            pw.println("    > APPARTEMENT " + app.getId());
                            for (Piece p : app.getPieces()) if (p != null) ecrirePiece(pw, p, n.getHauteur());
                        }
                    }
                }
                zoneTexte.setText("Devis détaillé exporté avec succès !");
            } catch (Exception e) { zoneTexte.setText("Erreur lors de l'exportation du fichier."); }
        }
    }

    private void ecrirePiece(java.io.PrintWriter pw, Piece p, double hauteur) {
        pw.println("\n      PIÈCE n°" + p.getId() + " (Surface Sol Brut : " + String.format("%.2f", p.surfaceSol()) + " m2)");
        if (p.getSol().getRevetement() != null) {
            double surfaceSolNette = p.surfaceSol();
            if (p.getSol().getNbT() > 0) surfaceSolNette = p.getSol().surfaceNette(p.surfaceSol());
            double prixS = p.getSol().getRevetement().montant(surfaceSolNette);
            pw.println("        - Revêtement Sol : " + p.getSol().getRevetement().getDesignation() + " | Coût : " + String.format("%.2f", prixS) + " €");
        }
        if (p.getPlafond().getRevetement() != null) {
            double prixP = p.getPlafond().getRevetement().montant(p.surfaceSol());
            pw.println("        - Revêtement Plafond : " + p.getPlafond().getRevetement().getDesignation() + " | Coût : " + String.format("%.2f", prixP) + " €");
        }
        if (p.getSol().getNbT() > 0) pw.println("        - Note : Surface nette sol après trémies : " + String.format("%.2f", p.getSol().surfaceNette(p.surfaceSol())) + " m2");

        pw.println("        - Murs :");
        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i];
            if (m != null) {
                double surfaceBrute = m.longueur() * hauteur;
                double surfaceOuvertures = 0;
                for (int j = 0; j < m.getNbO(); j++) if (m.getOuvertures()[j] != null) surfaceOuvertures += m.getOuvertures()[j].surface();
                double surfaceNette = Math.max(0, surfaceBrute - surfaceOuvertures);
                
                pw.print("          * Mur " + (i+1) + " : Long: " + String.format("%.2f", m.longueur()) + "m | Surf brute: " + String.format("%.2f", surfaceBrute) + " m2");
                if (surfaceOuvertures > 0) pw.print(" | Ouvertures: -" + String.format("%.2f", surfaceOuvertures) + " m2 | Surf nette: " + String.format("%.2f", surfaceNette) + " m2");
                if (m.getRevetement() != null) pw.print(" | Revêtement : " + m.getRevetement().getDesignation() + " (" + String.format("%.2f", m.getRevetement().montant(surfaceNette)) + " €)");
                else pw.print(" | Aucun revêtement");
                pw.println();
            }
        }
        pw.println("        >> TOTAL POUR CETTE PIÈCE : " + String.format("%.2f", p.devisPiece(hauteur)) + " €");
    }

    private void afficherConsignes() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Consignes d'utilisation"); dialog.initOwner(zoneDessin.getScene().getWindow());
        TextArea txtConsigne = new TextArea(Consigne.getTexte());
        txtConsigne.setEditable(false); txtConsigne.setWrapText(true); txtConsigne.setPrefSize(550, 450);
        txtConsigne.setStyle("-fx-font-size: 14px;");
        dialog.getDialogPane().setContent(txtConsigne); dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}