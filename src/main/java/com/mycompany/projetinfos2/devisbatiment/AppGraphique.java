/* =========================================================================
 * NOM DE LA CLASSE : AppGraphique
 * DATE DE MISE À JOUR : 18 Mai 2026
 * CATÉGORIE TECHNIQUE : Interface JavaFX (ou Point d'entrée du programme (Méthode Main), Classe Abstraite (Modèle parent),Sous-classe (Héritage),Classe Métier (Composant du bâtiment)
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
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class AppGraphique extends Application {
    
    // --- DONNÉES ET ÉTAT DU PROJET ---
    private Batiment projetActuel;     // Contient tout le projet (soit une Maison, soit un Immeuble)
    private Piece pieceActuelle;       // La pièce sur laquelle on est en train de travailler
    private Niveau niveauActuel;       // L'étage sur lequel on ajoute des éléments (étage de calcul)
    private Niveau niveauAffiche;      // L'étage qu'on est en train de regarder sur le plan
    private Appartement appartActuel;  // L'appartement en cours de modification (Utile pour le mode Immeuble)
    
    // --- COMPTEURS AUTOMATIQUES ---
    // Servent à donner des identifiants uniques aux objets au fur et à mesure de leur création
    private int compteurMur = 1;
    private int compteurPiece = 1;
    private int compteurNiveau = 0;
    private int compteurAppart = 0;
    
    // --- VARIABLES POUR LE DESSIN ET LA SOURIS ---
    private boolean pieceEnCours = false; // Passe à vrai quand on crée une pièce, bloque certaines actions
    private TextField champTx1, champTy1, champTx2, champTy2; // Liens vers les cases du formulaire pour que la souris puisse écrire dedans
    private boolean saisieMurEnCours = false; // Vrai quand la fenêtre de création de mur est ouverte
    private boolean attendPremierClic = false; // Détermine si le prochain clic définit le départ ou l'arrivée d'un mur
    private boolean pointArriveeVerrouille = false; // Bloque le suivi de la souris quand on clique pour figer le point d'arrivée
    
    // --- PARAMÈTRES D'AFFICHAGE (ZOOM ET DÉPLACEMENT) ---
    private double zoom = 1.0;          // Facteur de zoom pour le plan de dessin
    private double panX = 100;          // Décalage horizontal du dessin (pour pouvoir bouger le plan)
    private double panY = 300;          // Décalage vertical du dessin
    private double lastMouseX, lastMouseY; // Garde en mémoire la position précédente de la souris pour le déplacement du plan
    
    // --- COMPOSANTS VISUELS ---
    private ArrayList<Revetement> catalogue = new ArrayList<>(); // Liste de tous les revêtements chargés depuis le fichier texte
    private Canvas zoneDessin;          // Zone de dessin interactive (Ardoise magique)
    private TextArea zoneTexte;         // Zone de notifications en bas à droite pour guider l'utilisateur
    private TreeView<String> arbreProjet; // L'explorateur de projet (arbre de gauche)
    
    // --- BOUTONS DE L'INTERFACE ---
    private Button btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne;

    /**
     * Méthode de démarrage obligatoire pour JavaFX. 
     * Elle construit toute la fenêtre et gère les événements (clics, mouvements).
     */
    @Override
    public void start(Stage primaryStage) {
        chargerCatalogue("CatalogueRevetements.txt"); // On charge les prix dès le lancement
        
        primaryStage.setTitle("Calculateur de Devis Bâtiment - Version Graphique");
        
        // --- CRÉATION DE LA ZONE DE DESSIN ---
        zoneDessin = new Canvas(800, 600);
        actualiserDessin(); // Premier dessin à vide pour afficher la grille de repère
        
        // --- INTERACTION : SUIVI DE LA SOURIS POUR LES COORDONNÉES EN DIRECT ---
        zoneDessin.setOnMouseMoved(e -> {
            if (saisieMurEnCours) {
                // On convertit les pixels de l'écran en mètres réels selon le zoom et le décalage du plan
                double mx = Math.round(((e.getX() - panX) / (50 * zoom)) * 100.0) / 100.0;
                double my = Math.round(((e.getY() - panY) / (50 * zoom)) * 100.0) / 100.0;

                if (attendPremierClic) {
                    // Si c'est le premier mur de la pièce, le mouvement bouge le point de départ
                    champTx1.setText(String.valueOf(mx));
                    champTy1.setText(String.valueOf(my));
                } else if (!pointArriveeVerrouille) {
                    // Système d'aimant automatique : si on frôle le tout premier point (à moins de 40cm), on s'y accroche
                    if (pieceActuelle != null && pieceActuelle.getMurs()[0] != null) {
                        Coin origine = pieceActuelle.getMurs()[0].getDebut();
                        double dist = Math.sqrt(Math.pow(mx - origine.getCx(), 2) + Math.pow(my - origine.getCy(), 2));
                        if (dist < 0.4) {
                            mx = origine.getCx();
                            my = origine.getCy();
                        }
                    }
                    // Met à jour les cases du point d'arrivée en direct
                    champTx2.setText(String.valueOf(mx));
                    champTy2.setText(String.valueOf(my));
                }
            }
        });

        // --- INTERACTION : CLIC SOURIS SUR LE PLAN POUR VERROUILLER ---
        zoneDessin.setOnMouseClicked(e -> {
            if (saisieMurEnCours) {
                if (attendPremierClic) {
                    // On valide le point de départ, le prochain mouvement gérera le point d'arrivée
                    attendPremierClic = false;
                    pointArriveeVerrouille = false;
                    zoneTexte.setText("Départ fixé ! Bougez la souris pour l'arrivée et cliquez pour verrouiller.");
                } else {
                    // Alterne entre figer les coordonnées et libérer le suivi de la souris
                    pointArriveeVerrouille = !pointArriveeVerrouille;
                    
                    if (pointArriveeVerrouille) {
                        double mx = Double.parseDouble(champTx2.getText());
                        double my = Double.parseDouble(champTy2.getText());
                        if (pieceActuelle.getMurs()[0] != null) {
                            Coin origine = pieceActuelle.getMurs()[0].getDebut();
                            // Détection visuelle si on ferme la pièce
                            if (mx == origine.getCx() && my == origine.getCy()) {
                                zoneTexte.setText("Fermeture détectée et verrouillée ! Valisez le formulaire.");
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

        // --- GESTION DU DÉPLACEMENT DU PLAN (PANNING) AVEC LE CLIC DROIT OU GLISSÉ ---
        zoneDessin.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });
        
        zoneDessin.setOnMouseDragged(e -> {
            if (!saisieMurEnCours) { // On interdit de bouger le plan pendant qu'on trace un mur pour éviter les bugs
                panX += (e.getX() - lastMouseX);
                panY += (e.getY() - lastMouseY);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                actualiserDessin();
            }
        });
        
        // --- GESTION DU ZOOM AVEC LA MOLETTE DE LA SOURIS ---
        zoneDessin.setOnScroll(e -> {
            double ancienZoom = zoom;
            if (e.getDeltaY() > 0) zoom *= 1.1; // Molette vers le haut = Zoom avant
            else zoom /= 1.1;                   // Molette vers le bas = Zoom arrière
            zoom = Math.max(0.2, Math.min(zoom, 5.0)); // Sécurité pour pas zoomer à l'infini
            actualiserDessin();
        });

        // --- INTERACTION : SÉLECTION DANS L'ARBRE (EXPLORATEUR DE GAUCHE) ---
        arbreProjet = new TreeView<>();
        arbreProjet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getValue().startsWith("Niveau ")) {
                try {
                    // Permet de changer d'étage visuellement en cliquant sur l'arbre de gauche
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
        btnNouveau = new Button("🏗️ Nouveau Projet");
        btnAjoutNiveau = new Button("➕ Ajouter un Niveau");
        btnAjoutAppart = new Button("🚪 Ajouter un Appartement");
        btnNouvellePiece = new Button("📐 Nouvelle Pièce");
        btnAjouterMur = new Button("🧱 Ajouter un Mur");
        btnTerminerPiece = new Button("🛑 Forcer Fin Pièce");
        btnCalculer = new Button("🧮 Calculer le Devis");
        btnVoirCatalogue = new Button("📖 Voir le Catalogue");
        btnExporter = new Button("💾 Exporter le Devis");
        btnConsigne = new Button("💡 Consignes d'utilisation");
        
        // Aligne automatiquement la largeur de tous les boutons de la colonne
        Button[] tousLesBoutons = {btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne};
        for(Button b : tousLesBoutons) b.setMaxWidth(Double.MAX_VALUE);

        // --- CONFIGURATION DES ACTIONS DES BOUTONS ---
        btnNouveau.setOnAction(e -> initProjet());
        btnAjoutNiveau.setOnAction(e -> {
            if (projetActuel instanceof Immeuble) {
                compteurAppart = 0; // On remet à zéro pour le nouvel étage
                Niveau n = new Niveau(++compteurNiveau, 2.50, 10);
                ((Immeuble) projetActuel).ajouterNiveau(n);
                niveauActuel = n;
                niveauAffiche = n;
                appartActuel = null;
                zoneTexte.setText("Niveau " + compteurNiveau + " ajouté ! Ajoutez un appartement.");
                actualiserArbre();
                majBoutons();
            }
        });
        btnAjoutAppart.setOnAction(e -> {
            if (niveauActuel != null) {
                Appartement a = new Appartement(++compteurAppart, 20);
                niveauActuel.ajouterAppart(a);
                appartActuel = a;
                zoneTexte.setText("Appartement " + compteurAppart + " créé ! Créez une pièce maintenant.");
                actualiserArbre();
                majBoutons();
            }
        });
        btnNouvellePiece.setOnAction(e -> {
            pieceActuelle = new Piece(compteurPiece++, 50); // Tableau de 50 murs max par pièce
            pieceEnCours = true;
            compteurMur = 1;
            zoneTexte.setText("Pièce initialisée. Cliquez sur 'Ajouter un mur' pour dessiner.");
            majBoutons();
        });
        btnAjouterMur.setOnAction(e -> ouvrirFormulaireMur());
        btnTerminerPiece.setOnAction(e -> finaliserPiece());
        
        // BOUTON CALCULER : Calcule la somme HT, applique les 20% de TVA et donne le TTC
        btnCalculer.setOnAction(e -> {
            double totalHT = projetActuel.devisBatiment();
            double tva = totalHT * 0.20; 
            double totalTTC = totalHT + tva;
            
            zoneTexte.setText(
                "=== RÉCAPITULATIF DU DEVIS ===\n" +
                "Montant HT  : " + String.format("%.2f", totalHT) + " €\n" +
                "TVA (20%)   : " + String.format("%.2f", tva) + " €\n" +
                "Montant TTC : " + String.format("%.2f", totalTTC) + " €"
            );
        });
        
        btnVoirCatalogue.setOnAction(e -> {
            Dialog<Void> d = new Dialog<>();
            d.setTitle("Catalogue des Revêtements");
            TextArea t = new TextArea(); t.setEditable(false);
            for(Revetement r : catalogue) {
                t.appendText("ID " + r.getId() + " - " + r.getDesignation() + " (" + r.getPrix() + " €/m2)\n");
            }
            // Injection de la zone texte dans la fenêtre et ajout du bouton standard de fermeture
            d.getDialogPane().setContent(t); 
            d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            d.showAndWait();
        });
        
        btnConsigne.setOnAction(e -> afficherConsignes());
        btnExporter.setOnAction(e -> exporterDevis());

        // --- MISE EN PAGE GLOBALE (LAYOUT) ---
        zoneTexte = new TextArea("Bienvenue ! Créez un projet pour commencer.");
        zoneTexte.setEditable(false); zoneTexte.setPrefHeight(120);

        // VBox est un layout qui empile les éléments de haut en bas (ici avec 10px d'écart)
        VBox menuDroite = new VBox(10);
        menuDroite.setPadding(new Insets(10)); menuDroite.setPrefWidth(250);
        menuDroite.getChildren().addAll(new Label("Actions"), btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne, new Label("Explorateur détaillé :"), arbreProjet, zoneTexte);

        // Le BorderPane découpe la fenêtre en 5 zones (Haut, Bas, Centre, Gauche, Droite)
        BorderPane principal = new BorderPane();
        principal.setCenter(zoneDessin); // L'ardoise prend toute la place centrale
        principal.setRight(menuDroite);  // Le menu se cale proprement à droite

        majBoutons(); // Premier verrouillage de sécurité au démarrage

        primaryStage.setScene(new Scene(principal));
        primaryStage.show();
    }

    /**
     * Ouvre la boîte de dialogue pour paramétrer le projet (Maison ou Immeuble).
     */
    private void initProjet() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paramètres du Nouveau Projet");
        
        TextField txtNom = new TextField("MonProjet");
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Maison (Plain-pied)", "Immeuble / Étage");
        cbType.getSelectionModel().selectFirst();
        
        // Le GridPane agit comme un tableau Excel pour aligner parfaitement les labels et les champs
        GridPane grid = new GridPane(); 
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        grid.add(new Label("Nom du projet :"), 0, 0); grid.add(txtNom, 1, 0);
        grid.add(new Label("Type de bâtiment :"), 0, 1); grid.add(cbType, 1, 1);
        
        // C'est ici qu'on "branche" notre grille dans la fenêtre, avec les boutons OK et Annuler
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Remise à zéro complète de tous les index de l'application
                compteurPiece = 1; compteurNiveau = 0; compteurAppart = 0;
                pieceEnCours = false; pieceActuelle = null; appartActuel = null;
                
                if (cbType.getSelectionModel().getSelectedIndex() == 0) {
                    projetActuel = new Maison(txtNom.getText(), 100);
                    niveauActuel = null; niveauAffiche = null;
                    zoneTexte.setText("Projet Maison créé. Ajoutez directement une pièce !");
                } else {
                    projetActuel = new Immeuble(txtNom.getText(), 50);
                    Niveau n0 = new Niveau(0, 2.50, 10); // RDC automatique
                    ((Immeuble) projetActuel).ajouterNiveau(n0);
                    niveauActuel = n0; niveauAffiche = n0;
                    zoneTexte.setText("Projet Immeuble créé. RDC configuré (Niveau 0). Ajoutez un appartement.");
                }
                actualiserDessin(); actualiserArbre(); majBoutons();
            }
        });
    }

    /**
     * Ouvre la fenêtre pour dessiner un mur. La fenêtre est configurée pour rester
     * au-dessus du plan sans bloquer les clics de la souris.
     */
    private void ouvrirFormulaireMur() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Mur");
        
        // Modality.NONE rend la fenêtre "flottante" (non bloquante) : on peut toujours cliquer sur le Canvas en dessous
        dialog.initModality(javafx.stage.Modality.NONE);
        // Associe le pop-up à l'application principale pour l'empêcher de disparaître en arrière-plan
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        // On cherche si un mur existe déjà pour chaîner les coordonnées automatiquement
        Mur dernierMur = null;
        for (Mur m : pieceActuelle.getMurs()) if (m != null) dernierMur = m;
        TextField tx1 = new TextField(), ty1 = new TextField(), tx2 = new TextField(), ty2 = new TextField();
        
        if (dernierMur != null) {
            // Le départ du mur est obligatoirement la fin du mur d'avant
            tx1.setText(String.valueOf(dernierMur.getFin().getCx())); 
            ty1.setText(String.valueOf(dernierMur.getFin().getCy()));
            // On bloque la saisie des cases de départ
            tx1.setEditable(false); ty1.setEditable(false);
            tx1.setStyle("-fx-background-color: #eeeeee;"); ty1.setStyle("-fx-background-color: #eeeeee;");
            attendPremierClic = false; 
            zoneTexte.setText("Bougez la souris pour placer l'arrivée du mur, puis cliquez pour verrouiller.");
        } else {
            // Premier mur : tout est libre
            attendPremierClic = true; 
            zoneTexte.setText("1er mur : Bougez la souris et cliquez pour fixer le DÉPART.");
        }

        // On branche nos variables globales sur les cases de texte de ce formulaire
        champTx1 = tx1; champTy1 = ty1; champTx2 = tx2; champTy2 = ty2;
        saisieMurEnCours = true;
        pointArriveeVerrouille = false;
        btnAjouterMur.setDisable(true); // Sécurité pour éviter d'ouvrir deux fenêtres en même temps

        TextField tp = new TextField("0"), tf = new TextField("0");
        ComboBox<String> cb = new ComboBox<>(); cb.getItems().add("Aucun");
        for (Revetement r : catalogue) if (r.estPourMur()) cb.getItems().add(r.getDesignation());
        cb.getSelectionModel().selectFirst();
        
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Début X, Y (m):"), 0, 0); g.add(tx1, 1, 0); g.add(ty1, 2, 0);
        g.add(new Label("Fin X, Y (m):"), 0, 1); g.add(tx2, 1, 1); g.add(ty2, 2, 1);
        g.add(new Label("Nombre de Portes / Fenêtres :"), 0, 2); g.add(tp, 1, 2); g.add(tf, 2, 2);
        g.add(new Label("Revêtement mural :"), 0, 3); g.add(cb, 1, 3, 2, 1);
        dialog.getDialogPane().setContent(g); 
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // On récupère le bouton physique OK généré par JavaFX pour pouvoir le manipuler (le griser)
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setDisable(true);
        
        // Écouteur en temps réel (via Runnable) pour n'activer le bouton OK que si toutes les cases sont remplies
        Runnable verif = () -> btnOk.setDisable(tx2.getText().isEmpty() || ty2.getText().isEmpty() || tx1.getText().isEmpty() || ty1.getText().isEmpty());
        tx1.textProperty().addListener((o, old, n) -> verif.run()); ty1.textProperty().addListener((o, old, n) -> verif.run());
        tx2.textProperty().addListener((o, old, n) -> verif.run()); ty2.textProperty().addListener((o, old, n) -> verif.run());

        dialog.showAndWait().ifPresent(r -> {
            saisieMurEnCours = false; 
            if (r == ButtonType.OK) {
                try {
                    Mur m = new Mur(compteurMur++, new Coin(1, Double.parseDouble(tx1.getText()), Double.parseDouble(ty1.getText())), 
                                   new Coin(2, Double.parseDouble(tx2.getText()), Double.parseDouble(ty2.getText())), 20);
                    
                    // Ajout des ouvertures polymorphes (Portes et Fenêtres)
                    for (int i=0; i<Integer.parseInt(tp.getText()); i++) m.ajouterOuverture(new Porte(i));
                    for (int i=0; i<Integer.parseInt(tf.getText()); i++) m.ajouterOuverture(new Fenetre(i));
                    
                    // Application du revêtement s'il y en a un de sélectionné
                    if (!cb.getValue().equals("Aucun")) {
                        for (Revetement rev : catalogue) if (cb.getValue().equals(rev.getDesignation())) m.appliquerRevetement(rev);
                    }
                    
                    pieceActuelle.ajouterMur(m);
                    
                    // Vérification automatique de bouclage : si le point d'arrivée retombe sur le point de départ initial
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
        saisieMurEnCours = false; // Sécurité au cas où l'utilisateur ferme avec la croix rouge
        majBoutons();
    }

    /**
     * Clôture la pièce actuelle et propose le choix des revêtements pour le Sol et le Plafond.
     */
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
        
        // Branchement de la grille dans l'interface de la fenêtre modale
        dialog.getDialogPane().setContent(g); 
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        
        dialog.showAndWait().ifPresent(r -> {
            // Enregistrement des revêtements choisis
            for(Revetement rev : catalogue) {
                if(cbSol.getValue().equals(rev.getDesignation())) pieceActuelle.getSol().appliquerRevetement(rev);
                if(cbPlat.getValue().equals(rev.getDesignation())) pieceActuelle.getPlafond().appliquerRevetement(rev);
            }
            
            // On range la pièce au bon endroit selon la structure du bâtiment
            if (projetActuel instanceof Maison) {
                ((Maison) projetActuel).ajouterPiece(pieceActuelle);
            } else if (appartActuel != null) {
                appartActuel.ajouterPiece(pieceActuelle);
            }
            
            pieceEnCours = false;
            pieceActuelle = null;
            zoneTexte.setText("Pièce enregistrée avec succès !");
            actualiserDessin(); actualiserArbre(); majBoutons();
        });
    }

    /**
     * Efface le Canvas et redessine entièrement la grille ainsi que tous les murs de l'étage affiché.
     */
    private void actualiserDessin() {
        // Le GraphicsContext est l'outil JavaFX "pinceau" qui trace concrètement les formes sur le Canvas
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        gc.clearRect(0, 0, zoneDessin.getWidth(), zoneDessin.getHeight());
        
        // --- DESSIN DE LA GRILLE DE FOND (AIDE VISUELLE) ---
        gc.setStroke(Color.web("#e0e0e0")); gc.setLineWidth(0.5);
        double tailleCase = 50 * zoom;
        for (double x = panX % tailleCase; x < zoneDessin.getWidth(); x += tailleCase) gc.strokeLine(x, 0, x, zoneDessin.getHeight());
        for (double y = panY % tailleCase; y < zoneDessin.getHeight(); y += tailleCase) gc.strokeLine(0, y, zoneDessin.getWidth(), y);
        
        // Repère du point d'origine central (0,0) de notre repère en mètres
        gc.setStroke(Color.web("#b0b0b0")); gc.setLineWidth(1.5);
        gc.strokeLine(panX, 0, panX, zoneDessin.getHeight());
        gc.strokeLine(0, panY, zoneDessin.getWidth(), panY);
        
        // --- DESSIN DES MURS DU PROJET ---
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
        
        // Si on est en train de tracer une pièce, on dessine en bleu fluo les segments déjà fixés
        if (pieceEnCours && pieceActuelle != null) {
            gc.setStroke(Color.DEEPSKYBLUE); gc.setLineWidth(4.0);
            dessinerMursPiece(gc, pieceActuelle);
        }
    }

    /**
     * Convertit les coordonnées mathématiques réelles en pixels écran pour dessiner les lignes.
     */
    private void dessinerMursPiece(GraphicsContext gc, Piece p) {
        for (Mur m : p.getMurs()) {
            if (m != null) {
                // Transformation géométrique : (position en m * echelle pixels * zoom) + le décalage de la caméra
                double x1 = m.getDebut().getCx() * (50 * zoom) + panX;
                double y1 = m.getDebut().getCy() * (50 * zoom) + panY;
                double x2 = m.getFin().getCx() * (50 * zoom) + panX;
                double y2 = m.getFin().getCy() * (50 * zoom) + panY;
                
                gc.strokeLine(x1, y1, x2, y2);
                gc.fillOval(x1-3, y1-3, 6, 6); // Petit point sur les intersections
            }
        }
    }

    /**
     * Met à jour dynamiquement l'explorateur (le TreeView à gauche) pour refléter l'architecture du projet.
     */
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
        arbreProjet.setRoot(racine); // Applique notre nouvel arbre généré dans le composant visuel final
    }

    /**
     * Système de sécurité de l'interface : active ou désactive les boutons selon l'état actuel 
     * de la saisie pour empêcher l'utilisateur de cliquer là où il ne faut pas.
     */
    private void majBoutons() {
        boolean aProjet = (projetActuel != null);
        boolean estImmeuble = aProjet && (projetActuel instanceof Immeuble);
        
        btnAjoutNiveau.setDisable(!estImmeuble || pieceEnCours);
        btnAjoutAppart.setDisable(!estImmeuble || niveauActuel == null || pieceEnCours);
        
        if (estImmeuble) {
            btnNouvellePiece.setDisable(appartActuel == null || pieceEnCours);
        } else {
            btnNouvellePiece.setDisable(!aProjet || pieceEnCours);
        }
        
        btnAjouterMur.setDisable(!pieceEnCours || saisieMurEnCours);
        btnTerminerPiece.setDisable(!pieceEnCours || compteurMur < 3);
        btnCalculer.setDisable(!aProjet || pieceEnCours);
        btnVoirCatalogue.setDisable(pieceEnCours);
        btnExporter.setDisable(!aProjet || pieceEnCours);
        btnConsigne.setDisable(pieceEnCours);
    }

    /**
     * Parse (analyse) le catalogue textuel pour remplir la structure mémoire Java.
     */
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

    /**
     * Crée le fichier de sortie .txt contenant le devis financier ultra exhaustif.
     */
    private void exporterDevis() {
        // FileChooser est une classe JavaFX qui ouvre l'explorateur de fichiers natif de l'ordinateur
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
                    for (Piece p : ((Maison)projetActuel).getPieces()) {
                        if (p != null) ecrirePiece(pw, p, 2.5); // 2.5m de hauteur par défaut pour une maison
                    }
                } else {
                    for (Niveau n : ((Immeuble)projetActuel).getNiveaux()) {
                        if (n == null) continue;
                        pw.println("\n>>> NIVEAU " + n.getId() + " [HSP : " + n.getHauteur() + "m]");
                        for (Appartement app : n.getApparts()) {
                            if (app == null) continue;
                            pw.println("    > APPARTEMENT " + app.getId());
                            for (Piece p : app.getPieces()) {
                                if (p != null) ecrirePiece(pw, p, n.getHauteur());
                            }
                        }
                    }
                }
                zoneTexte.setText("Devis détaillé exporté avec succès !");
            } catch (Exception e) { 
                zoneTexte.setText("Erreur lors de l'exportation du fichier."); 
            }
        }
    }

    /**
     * Calcule et écrit le détail ligne par ligne de chaque composant d'une pièce dans le fichier devis.
     */
    private void ecrirePiece(java.io.PrintWriter pw, Piece p, double hauteur) {
        pw.println("\n      PIÈCE n°" + p.getId() + " (Surface Sol Brut : " + String.format("%.2f", p.surfaceSol()) + " m2)");
        
        // --- Traitement Sol ---
        if (p.getSol().getRevetement() != null) {
            double surfaceSolNette = p.surfaceSol();
            if (p.getSol().getNbT() > 0) surfaceSolNette = p.getSol().surfaceNette(p.surfaceSol());
            double prixS = p.getSol().getRevetement().montant(surfaceSolNette);
            pw.println("        - Revêtement Sol : " + p.getSol().getRevetement().getDesignation() + " | Coût : " + String.format("%.2f", prixS) + " €");
        }
        
        // --- Traitement Plafond ---
        if (p.getPlafond().getRevetement() != null) {
            double prixP = p.getPlafond().getRevetement().montant(p.surfaceSol());
            pw.println("        - Revêtement Plafond : " + p.getPlafond().getRevetement().getDesignation() + " | Coût : " + String.format("%.2f", prixP) + " €");
        }

        if (p.getSol().getNbT() > 0) {
            pw.println("        - Note : Surface nette sol après trémies : " + String.format("%.2f", p.getSol().surfaceNette(p.surfaceSol())) + " m2");
        }

        // --- Traitement individualisé des Murs (Soustraction des portes/fenêtres) ---
        pw.println("        - Murs :");
        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i];
            if (m != null) {
                double surfaceBrute = m.longueur() * hauteur;
                double surfaceOuvertures = 0;
                
                // Cumul des surfaces d'ouvertures portées par ce mur via le polymorphisme (.surface())
                for (int j = 0; j < m.getNbO(); j++) {
                    if (m.getOuvertures()[j] != null) {
                        surfaceOuvertures += m.getOuvertures()[j].surface();
                    }
                }
                
                double surfaceNette = Math.max(0, surfaceBrute - surfaceOuvertures);
                pw.print("          * Mur " + (i+1) + " : Long: " + String.format("%.2f", m.longueur()) + "m | Surf brute: " + String.format("%.2f", surfaceBrute) + " m2");
                
                if (surfaceOuvertures > 0) {
                    pw.print(" | Ouvertures: -" + String.format("%.2f", surfaceOuvertures) + " m2 | Surf nette: " + String.format("%.2f", surfaceNette) + " m2");
                }
                
                if (m.getRevetement() != null) {
                    double prixM = m.getRevetement().montant(surfaceNette);
                    pw.print(" | Revêtement : " + m.getRevetement().getDesignation() + " (" + String.format("%.2f", prixM) + " €)");
                } else {
                    pw.print(" | Aucun revêtement");
                }
                pw.println();
            }
        }
        pw.println("        >> TOTAL POUR CETTE PIÈCE : " + String.format("%.2f", p.devisPiece(hauteur)) + " €");
    }

    /**
     * Appelle le module de documentation et affiche l'aide utilisateur.
     */
    private void afficherConsignes() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Consignes d'utilisation");
        dialog.initOwner(zoneDessin.getScene().getWindow());

        TextArea txtConsigne = new TextArea(Consigne.getTexte());
        txtConsigne.setEditable(false);
        txtConsigne.setWrapText(true);
        txtConsigne.setPrefSize(550, 450);
        txtConsigne.setStyle("-fx-font-size: 14px;");

        dialog.getDialogPane().setContent(txtConsigne);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}