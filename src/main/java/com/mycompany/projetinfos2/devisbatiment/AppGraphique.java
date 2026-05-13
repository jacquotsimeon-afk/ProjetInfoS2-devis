package com.mycompany.projetinfos2.devisbatiment;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class AppGraphique extends Application {
    
    // --- DONNÉES DU PROJET ---
    private Batiment projetActuel;
    private Piece pieceActuelle;
    private Niveau niveauActuel;      // Le niveau où l'on travaille
    private Niveau niveauAffiche;      // Le niveau que l'on voit à l'écran
    private Appartement appartActuel;
    
    private int compteurMur = 1;
    private int compteurPiece = 1;
    private int compteurNiveau = 0;
    private int compteurAppart = 0;
    private boolean pieceEnCours = false;
    
    private ArrayList<Revetement> catalogue = new ArrayList<>();
    private Canvas zoneDessin;
    private TextArea zoneTexte;
    private TreeView<String> arbreProjet;
    
    private Button btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer;

    private double panX = 50, panY = 50;
    private double zoom = 1.0; // NOUVELLE VARIABLE POUR LE ZOOM
    private double lastMouseX, lastMouseY;

    @Override
    public void start(Stage primaryStage) {
        chargerCatalogue();

        // --- 1. ZONE DE DESSIN ---
        zoneDessin = new Canvas(600, 600);
        Pane conteneurDessin = new Pane(zoneDessin);
        conteneurDessin.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        
        // MODIFICATION : Verrouillage de la taille pour empêcher le dépassement à droite
        zoneDessin.widthProperty().bind(conteneurDessin.widthProperty());
        zoneDessin.heightProperty().bind(conteneurDessin.heightProperty());
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(conteneurDessin.widthProperty());
        clip.heightProperty().bind(conteneurDessin.heightProperty());
        conteneurDessin.setClip(clip);
        
        // Redessiner automatiquement si la fenêtre change de taille
        zoneDessin.widthProperty().addListener(e -> actualiserDessin());
        zoneDessin.heightProperty().addListener(e -> actualiserDessin());

        zoneDessin.setOnMousePressed(e -> { lastMouseX = e.getX(); lastMouseY = e.getY(); });
        zoneDessin.setOnMouseDragged(e -> {
            panX += e.getX() - lastMouseX; panY += e.getY() - lastMouseY;
            lastMouseX = e.getX(); lastMouseY = e.getY();
            actualiserDessin();
        });
        
        // MODIFICATION : Ajout du Zoom avec la molette de la souris
        zoneDessin.setOnScroll(e -> {
            if (e.getDeltaY() > 0) zoom *= 1.1; // Zoom avant
            else zoom /= 1.1;                   // Zoom arrière
            actualiserDessin();
        });
        
        dessinerGrilleVierge();

        // --- 2. MENU GAUCHE ---
        VBox menuGauche = new VBox(20);
        menuGauche.setPadding(new Insets(15));
        menuGauche.setPrefWidth(200);
        menuGauche.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 0 1 0 0;");
        Label lblLegende = new Label("LÉGENDE");
        lblLegende.setStyle("-fx-font-weight: bold;");
        menuGauche.getChildren().addAll(lblLegende, 
            creerItemLegende(new Line(0, 0, 20, 0), Color.DARKBLUE, "Mur"),
            creerItemLegende(new Circle(6), Color.SADDLEBROWN, "Porte"),
            creerItemLegende(new Circle(6), Color.LIGHTSKYBLUE, "Fenêtre"),
            creerItemLegende(new Circle(4), Color.RED, "Origine (0,0)")
        );

        // --- 3. MENU DROIT ---
        VBox menuDroite = new VBox(10);
        menuDroite.setPadding(new Insets(15));
        menuDroite.setPrefWidth(320);
        btnNouveau = new Button("1. Nouveau Projet");
        btnAjoutNiveau = new Button("➕ Ajouter un Niveau");
        btnAjoutAppart = new Button("🏠 Ajouter un Appartement");
        btnNouvellePiece = new Button("2. Nouvelle Pièce");
        btnAjouterMur = new Button("3. Ajouter un Mur");
        btnTerminerPiece = new Button("✔ Terminer la Pièce");
        btnCalculer = new Button("4. Calculer le Devis");
        
        Button[] tousLesBoutons = {btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer};
        for(Button b : tousLesBoutons) b.setMaxWidth(Double.MAX_VALUE);

        arbreProjet = new TreeView<>();
        arbreProjet.setPrefHeight(300);
        
        // --- LOGIQUE DE SELECTION DANS L'ARBORESCENCE ---
        arbreProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !pieceEnCours) {
                String texte = newVal.getValue();
                // Si on clique sur un niveau (ou un enfant d'un niveau), on change de vue
                identifierEtAfficherNiveau(newVal);
            }
        });

        zoneTexte = new TextArea("Prêt.");
        zoneTexte.setEditable(false); zoneTexte.setWrapText(true); zoneTexte.setPrefHeight(100);

        menuDroite.getChildren().addAll(new Label("Actions"), btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, new Label("Explorateur détaillé :"), arbreProjet, zoneTexte);

        // --- ACTIONS ---
        btnNouveau.setOnAction(e -> ouvrirFormulaireNouveauProjet());
        
        btnAjoutNiveau.setOnAction(e -> {
            compteurNiveau++;
            niveauActuel = new Niveau(compteurNiveau, 2.5, 10);
            ((Immeuble)projetActuel).ajouterNiveau(niveauActuel);
            niveauAffiche = niveauActuel; // On affiche le nouveau niveau
            appartActuel = null; 
            majBoutons();
            actualiserArbre();
            actualiserDessin();
            zoneTexte.setText("Niveau " + compteurNiveau + " ajouté.");
        });

        btnAjoutAppart.setOnAction(e -> {
            compteurAppart++;
            appartActuel = new Appartement(compteurAppart, 10);
            niveauActuel.ajouterAppart(appartActuel);
            actualiserArbre();
            majBoutons();
            zoneTexte.setText("Appartement " + compteurAppart + " créé.");
        });

        btnNouvellePiece.setOnAction(e -> {
            pieceEnCours = true;
            niveauAffiche = niveauActuel; // On force l'affichage du niveau de construction
            compteurPiece++;
            pieceActuelle = new Piece(compteurPiece, 50);
            if (projetActuel instanceof Maison) ((Maison)projetActuel).ajouterPiece(pieceActuelle);
            else appartActuel.ajouterPiece(pieceActuelle);
            compteurMur = 1;
            majBoutons();
            actualiserArbre();
            actualiserDessin();
            zoneTexte.setText("Construction : Pièce " + compteurPiece);
        });

        btnAjouterMur.setOnAction(e -> ouvrirFormulaireMur());
        btnTerminerPiece.setOnAction(e -> finaliserPiece());

        btnCalculer.setOnAction(e -> {
            double total = projetActuel.devisBatiment();
            zoneTexte.setText("DEVIS TOTAL : " + String.format("%.2f", total) + " €");
        });

        majBoutons();
        BorderPane layoutPrincipal = new BorderPane();
        layoutPrincipal.setLeft(menuGauche);
        layoutPrincipal.setCenter(conteneurDessin);
        layoutPrincipal.setRight(menuDroite);

        primaryStage.setScene(new Scene(layoutPrincipal, 1050, 650));
        primaryStage.setTitle("Construction 2D du plan et devis");
        primaryStage.show();
        
    }

    // --- LOGIQUE DE NAVIGATION ---
    private void identifierEtAfficherNiveau(TreeItem<String> item) {
        TreeItem<String> current = item;
        // On remonte l'arbre pour trouver le parent "Niveau X"
        while (current != null) {
            if (current.getValue().startsWith("Niveau ")) {
                int idSelectionne = Integer.parseInt(current.getValue().substring(7));
                if (projetActuel instanceof Immeuble) {
                    for (Niveau n : ((Immeuble)projetActuel).getNiveaux()) {
                        if (n != null && n.getId() == idSelectionne) {
                            niveauAffiche = n;
                            actualiserDessin();
                            zoneTexte.setText("Affichage du Niveau " + idSelectionne);
                            return;
                        }
                    }
                }
            }
            current = current.getParent();
        }
    }

    private void majBoutons() {
        boolean aProjet = (projetActuel != null);
        boolean estImmeuble = (projetActuel instanceof Immeuble);
        btnAjoutNiveau.setVisible(estImmeuble); btnAjoutNiveau.setManaged(estImmeuble);
        btnAjoutAppart.setVisible(estImmeuble); btnAjoutAppart.setManaged(estImmeuble);
        btnAjoutNiveau.setDisable(!aProjet || pieceEnCours);
        btnAjoutAppart.setDisable(!aProjet || pieceEnCours || niveauActuel == null);
        boolean peutCreerPiece = aProjet && ((estImmeuble && appartActuel != null) || !estImmeuble);
        btnNouvellePiece.setDisable(!peutCreerPiece || pieceEnCours);
        btnAjouterMur.setDisable(!aProjet || !pieceEnCours);
        btnTerminerPiece.setDisable(!aProjet || !pieceEnCours);
        btnCalculer.setDisable(!aProjet || pieceEnCours);
    }

    private void ouvrirFormulaireNouveauProjet() {
        Dialog<ButtonType> d = new Dialog<>(); d.setTitle("Nouveau Projet");
        TextField tn = new TextField("Mon Projet"); 
        
        // MODIFICATION ICI : Ajout des descriptions
        ComboBox<String> ct = new ComboBox<>(); 
        ct.getItems().addAll("Maison (1 seul niveau)", "Immeuble (plusieurs niveaux)"); 
        ct.getSelectionModel().selectFirst();
        
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Nom :"), 0, 0); g.add(tn, 1, 0); g.add(new Label("Type :"), 0, 1); g.add(ct, 1, 1);
        d.getDialogPane().setContent(g); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.showAndWait().ifPresent(r -> { 
            if (r == ButtonType.OK) {
                compteurPiece = 0; pieceEnCours = false;
                
                // MODIFICATION ICI : Utilisation de startsWith pour s'adapter à la description
                if (ct.getValue().startsWith("Maison")) {
                    projetActuel = new Maison(tn.getText(), 50);
                    niveauAffiche = null;
                } else {
                    projetActuel = new Immeuble(tn.getText(), 10);
                    niveauActuel = new Niveau(0, 2.5, 10);
                    ((Immeuble)projetActuel).ajouterNiveau(niveauActuel);
                    niveauAffiche = niveauActuel; 
                    appartActuel = null; 
                }
                panX = 50; panY = 50; 
                majBoutons(); actualiserArbre(); actualiserDessin();
            }
        });
    }

    private void ouvrirFormulaireMur() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Mur");
        Mur dernierMur = null;
        for (Mur m : pieceActuelle.getMurs()) if (m != null) dernierMur = m;
        TextField tx1 = new TextField(), ty1 = new TextField(), tx2 = new TextField(), ty2 = new TextField();
        if (dernierMur != null) {
            tx1.setText(String.valueOf(dernierMur.getFin().getCx())); ty1.setText(String.valueOf(dernierMur.getFin().getCy()));
            tx1.setEditable(false); ty1.setEditable(false);
        }
        TextField tp = new TextField("0"), tf = new TextField("0");
        ComboBox<String> cb = new ComboBox<>(); cb.getItems().add("Aucun");
        for (Revetement r : catalogue) if (r.estPourMur()) cb.getItems().add(r.getDesignation());
        cb.getSelectionModel().selectFirst();
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Début X, Y:"), 0, 0); g.add(tx1, 1, 0); g.add(ty1, 2, 0);
        g.add(new Label("Fin X, Y:"), 0, 1); g.add(tx2, 1, 1); g.add(ty2, 2, 1);
        
        // MODIFICATIONS ICI : Mots en entier
        g.add(new Label("Portes / Fenêtres :"), 0, 2); g.add(tp, 1, 2); g.add(tf, 2, 2);
        g.add(new Label("Revêtement :"), 0, 3); g.add(cb, 1, 3, 2, 1);
        
        dialog.getDialogPane().setContent(g); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setDisable(true);
        Runnable verif = () -> btnOk.setDisable(tx2.getText().isEmpty() || ty2.getText().isEmpty());
        tx2.textProperty().addListener((o, old, n) -> verif.run()); ty2.textProperty().addListener((o, old, n) -> verif.run());

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Mur m = new Mur(compteurMur++, new Coin(1, Double.parseDouble(tx1.getText()), Double.parseDouble(ty1.getText())), 
                                   new Coin(2, Double.parseDouble(tx2.getText()), Double.parseDouble(ty2.getText())), 20);
                    for (int i=0; i<Integer.parseInt(tp.getText()); i++) m.ajouterOuverture(new Porte(i));
                    for (int i=0; i<Integer.parseInt(tf.getText()); i++) m.ajouterOuverture(new Fenetre(i));
                    if (!cb.getValue().equals("Aucun")) for (Revetement rev : catalogue) if (cb.getValue().equals(rev.getDesignation())) m.appliquerRevetement(rev);
                    pieceActuelle.ajouterMur(m);
                    
                    Mur pre = pieceActuelle.getMurs()[0];
                    if (compteurMur > 3 && m.getFin().getCx() == pre.getDebut().getCx() && m.getFin().getCy() == pre.getDebut().getCy()) {
                        actualiserDessin(); actualiserArbre(); finaliserPiece();
                    } else {
                        actualiserDessin(); actualiserArbre();
                    }
                } catch (Exception ex) { zoneTexte.setText("Erreur de saisie."); }
            }
        });
    }

    private void finaliserPiece() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fin de pièce");
        dialog.setHeaderText("Finaliser la pièce : choisissez les revêtements ou recommencez.");

        // MODIFICATION ICI : Ajout des boutons Valider et Recommencer
        ButtonType btnValider = new ButtonType("Valider la pièce", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnRecommencer = new ButtonType("Recommencer la pièce", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, btnRecommencer);

        ComboBox<String> cbS = new ComboBox<>(), cbP = new ComboBox<>();
        cbS.getItems().add("Aucun"); cbP.getItems().add("Aucun");
        for (Revetement r : catalogue) {
            if (r.estPourSol()) cbS.getItems().add(r.getDesignation());
            if (r.estPourPlafond()) cbP.getItems().add(r.getDesignation());
        }
        cbS.getSelectionModel().selectFirst(); cbP.getSelectionModel().selectFirst();
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        g.add(new Label("Sol:"), 0, 0); g.add(cbS, 1, 0); g.add(new Label("Plafond:"), 0, 1); g.add(cbP, 1, 1);
        dialog.getDialogPane().setContent(g); 
        
        dialog.showAndWait().ifPresent(r -> {
            if (r == btnRecommencer) {
                // LOGIQUE DE RECONSTRUCTION
                if (projetActuel instanceof Maison) {
                    ((Maison)projetActuel).supprimerDernierePiece();
                } else if (appartActuel != null) {
                    appartActuel.supprimerDernierePiece();
                }
                compteurPiece--; // On recule le compteur pour le prochain essai
                pieceActuelle = null;
                pieceEnCours = false;
                zoneTexte.setText("Pièce annulée. Cliquez sur 'Nouvelle Pièce' pour recommencer.");
            } else {
                // LOGIQUE DE VALIDATION
                for (Revetement rev : catalogue) {
                    if (cbS.getValue().equals(rev.getDesignation())) pieceActuelle.getSol().appliquerRevetement(rev);
                    if (cbP.getValue().equals(rev.getDesignation())) pieceActuelle.getPlafond().appliquerRevetement(rev);
                }
                pieceEnCours = false; 
                zoneTexte.setText("Pièce " + pieceActuelle.getId() + " enregistrée avec succès.");
            }
            majBoutons(); actualiserDessin(); actualiserArbre();
        });
    }

    // --- LOGIQUE DE DESSIN FILTRÉE ---
    private void actualiserDessin() {
        dessinerGrilleVierge();
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        
        ArrayList<Piece> piecesAVoir = new ArrayList<>();
        if (projetActuel instanceof Maison) {
            for (Piece p : ((Maison)projetActuel).getPieces()) if (p != null) piecesAVoir.add(p);
        } else if (niveauAffiche != null) {
            // ON NE PREND QUE LES PIÈCES DU NIVEAU AFFICHÉ
            for (Appartement a : niveauAffiche.getApparts()) {
                if (a != null) for (Piece p : a.getPieces()) if (p != null) piecesAVoir.add(p);
            }
        }

        for (Piece p : piecesAVoir) {
            double mX = 0, mY = 0; int nP = 0;
            for (Mur m : p.getMurs()) {
                if (m != null) {
                    double x1 = toScreenX(m.getDebut().getCx()), y1 = toScreenY(m.getDebut().getCy());
                    double x2 = toScreenX(m.getFin().getCx()), y2 = toScreenY(m.getFin().getCy());
                    gc.setStroke(Color.DARKBLUE); gc.setLineWidth(4);
                    gc.strokeLine(x1, y1, x2, y2);
                    mX += x1; mY += y1; nP++;
                    int nbO = m.getNbO();
                    for (int i=1; i<=nbO; i++) {
                        gc.setFill(m.getOuvertures()[i-1] instanceof Porte ? Color.SADDLEBROWN : Color.LIGHTSKYBLUE);
                        double f = (double) i / (nbO + 1);
                        gc.fillOval(x1 + f*(x2-x1) - 6, y1 + f*(y2-y1) - 6, 12, 12);
                    }
                }
            }
            if (nP > 0) {
                gc.setFill(Color.BLACK); gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.fillText("Pièce " + p.getId(), (mX/nP)-20, (mY/nP));
            }
        }
    }

    private void actualiserArbre() {
        if (projetActuel == null) return;
        TreeItem<String> root = new TreeItem<>(projetActuel.getId()); root.setExpanded(true);
        if (projetActuel instanceof Maison) {
            for (Piece p : ((Maison)projetActuel).getPieces()) if (p != null) root.getChildren().add(creerNoeudPiece(p));
        } else {
            for (Niveau n : ((Immeuble)projetActuel).getNiveaux()) {
                if (n != null) {
                    // MODIFICATION ICI : Ajout de la hauteur (HSP)
                    TreeItem<String> nn = new TreeItem<>("Niveau " + n.getId() + " (HSP: " + n.getHauteur() + "m)"); nn.setExpanded(true);
                    for (Appartement a : n.getApparts()) {
                        if (a != null) {
                            TreeItem<String> na = new TreeItem<>("Appartement " + a.getId()); na.setExpanded(true);
                            for (Piece p : a.getPieces()) if (p != null) na.getChildren().add(creerNoeudPiece(p));
                            nn.getChildren().add(na);
                        }
                    }
                    root.getChildren().add(nn);
                }
            }
        }
        arbreProjet.setRoot(root);
    }

    private TreeItem<String> creerNoeudPiece(Piece p) {
        TreeItem<String> np = new TreeItem<>("Pièce " + p.getId());
        
        // MODIFICATION ICI : Mots en entier pour le sol
        if (p.getSol().getRevetement() != null) np.getChildren().add(new TreeItem<>("Revêtement Sol : " + p.getSol().getRevetement().getDesignation()));
        
        for (Mur m : p.getMurs()) {
            if (m != null) {
                TreeItem<String> nm = new TreeItem<>("Mur " + m.getId() + " (" + String.format("%.2f", m.longueur()) + "m)");
                
                // MODIFICATION ICI : Mot en entier pour le mur
                if (m.getRevetement() != null) nm.getChildren().add(new TreeItem<>("Revêtement : " + m.getRevetement().getDesignation()));
                
                np.getChildren().add(nm);
            }
        }
        return np;
    }

    // --- UTILITAIRES ---
    // MODIFICATION : Les mathématiques prennent en compte le zoom
    private double toScreenX(double m) { return (m * 50 * zoom) + panX; }
    private double toScreenY(double m) { return (m * 50 * zoom) + panY; }
    
    // MODIFICATION : La grille s'adapte à l'écran et au zoom
    private void dessinerGrilleVierge() {
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        double w = zoneDessin.getWidth();
        double h = zoneDessin.getHeight();
        
        gc.clearRect(0, 0, w, h); 
        gc.setStroke(Color.LIGHTGRAY);
        
        double tailleCarreau = 50 * zoom;
        for (double x = panX % tailleCarreau; x < w; x += tailleCarreau) gc.strokeLine(x, 0, x, h);
        for (double y = panY % tailleCarreau; y < h; y += tailleCarreau) gc.strokeLine(0, y, w, y);
        
        gc.setFill(Color.RED); 
        gc.fillOval(toScreenX(0)-4, toScreenY(0)-4, 8, 8);
    }
    private void chargerCatalogue() {
        try (BufferedReader br = new BufferedReader(new FileReader("CatalogueRevetements.txt"))) {
            br.readLine(); String l;
            while ((l = br.readLine()) != null) {
                String[] t = l.split(";");
                catalogue.add(new Peinture(Integer.parseInt(t[0]), t[1], Double.parseDouble(t[5]), t[2].equals("1"), t[3].equals("1"), t[4].equals("1")));
            }
        } catch (Exception e) {}
    }
    private HBox creerItemLegende(javafx.scene.shape.Shape f, Color c, String t) { f.setStroke(c); if (f instanceof Circle) f.setFill(c); HBox b = new HBox(10, f, new Label(t)); b.setAlignment(javafx.geometry.Pos.CENTER_LEFT); return b; }
    public static void main(String[] args) { launch(args); }
}

//