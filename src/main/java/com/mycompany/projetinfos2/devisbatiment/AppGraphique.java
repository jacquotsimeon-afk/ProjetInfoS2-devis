/* =========================================================================
 * NOM DE LA CLASSE : AppGraphique
 * DATE DE MISE À JOUR : 22 Mai 2026
 * CATÉGORIE TECHNIQUE : Interface Graphique (JavaFX - Fenêtre principale)
 * =========================================================================
 * DESCRIPTION :
 * Classe principale pour l'interface graphique JavaFX de l'application.
 * Elle permet de dessiner le bâtiment visuellement, choisir les revêtements, de générer le devis puis l'exporter.
 * =========================================================================
 * @author Siméon
 * =========================================================================
 */


package com.mycompany.projetinfos2.devisbatiment;

//Importation des bibliothèques JavaFX nécessaires pour l'interface graphique
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
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

//Importation des bibliothèques standards pour lire/écrire des fichiers et utiliser des listes
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

//La classe hérite de "Application", pour indiquer à Java que c'est une interface graphique
public class AppGraphique extends Application {
    
    //VARIABLES GLOBALES
    private Batiment projetActuel;  //Mémorise le projet global (On utilise le polymorphisme : peut être une Maison ou un Immeuble)   
    private Piece pieceActuelle;  //Mémorise la pièce en cours de création pour y ajouter des murs    
    private Niveau niveauActuel;  //Mémorise l'étage sur lequel l'utilisateur est en train de travailler (logique métier)     
    private Niveau niveauAffiche; //Mémorise l'étage que l'utilisateur a choisi de regarder sur le plan (logique visuelle)     
    private Appartement appartActuel; //Mémorise l'appartement sélectionné (utile uniquement si le projet est un immeuble) 
    
    //COMPTEURS : Servent à générer des identifiants uniques (1, 2, 3...) automatiquement
    private int compteurMur = 1;
    private int compteurPiece = 1;
    private int compteurNiveau = 0;
    private int compteurAppart = 0;
    
    //VARIABLES D'ÉTAT : Permettent de savoir ce que l'utilisateur est en train de faire
    private boolean pieceEnCours = false;  //Vrai si une pièce est ouverte (bloque la création d'autres éléments)
    private TextField champTx1, champTy1, champTx2, champTy2; //Références directes vers les cases de texte du formulaire pour y écrire avec la souris (mur)
    private boolean saisieMurEnCours = false; //Vrai si la fenêtre d'ajout de mur est ouverte
    private boolean attendPremierClic = false; //Vrai si le prochain clic de souris doit définir le début du mur (et non la fin)
    private boolean pointArriveeVerrouille = false; //Vrai si l'utilisateur a cliqué pour figer le point d'arrivée du mur
    
    //VARIABLES DE NAVIGATION (Caméra du plan)
    private double zoom = 1.0; //Gère le niveau de zoom (1.0 = 100%)        
    private double panX = 100; //Décalage horizontal (X) de la caméra par défaut         
    private double panY = 300; //Décalage vertical (Y) de la caméra par défaut         
    private double lastMouseX, lastMouseY; //Mémorise les coordonnées X et Y de la souris juste avant un mouvement (pour le glisser-déposer)
    
    //COMPOSANTS VISUELS PRINCIPAUX
    private ArrayList<Revetement> catalogue = new ArrayList<>(); //Liste dynamique qui va stocker tous les revêtements lus dans le fichier texte
    private Canvas zoneDessin; //Le Canvas est la zone de dessin (le plan) où l'on trace les lignes         
    private TextArea zoneTexte; //Zone de texte en bas à droite pour communiquer avec l'utilisateur       
    private TreeView<String> arbreProjet; //Composant en forme d'arborescence (dossiers/sous-dossiers) pour l'arbre à gauche
    
    //Déclaration de tous les boutons de l'interface
    private Button btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne;

    //MÉTHODE START : Le véritable point d'entrée de JavaFX (construit la fenêtre)
    @Override
    public void start(Stage primaryStage) {
        //Initialisation de la zone de notification utilisateur
        zoneTexte = new TextArea("Bienvenue ! Créez un projet pour commencer.");
        //Empêche l'utilisateur d'écrire dedans à la main
        zoneTexte.setEditable(false); 
        //Fixe la hauteur de cette zone à 120 pixels
        zoneTexte.setPrefHeight(120);
        //Force le texte à revenir à la ligne s'il dépasse la largeur de la boîte
        zoneTexte.setWrapText(true); 

        //Appel de la méthode qui lit le fichier texte des prix dès le lancement
        chargerCatalogue("CatalogueRevetements.txt"); 
        
        //Donne un titre à la fenêtre principale du logiciel
        primaryStage.setTitle("Calculateur de Devis Bâtiment - Version Graphique");
        
        //Instancie la zone de dessin avec une taille de base (800x600 pixels)
        zoneDessin = new Canvas(800, 600);
        //Fait un premier rendu visuel (affiche juste la grille grise vide pour l'instant)
        actualiserDessin(); 
        
        //ÉVÉNEMENT : Que faire quand la souris BOUGE au-dessus de la zone de dessin ?
        zoneDessin.setOnMouseMoved(e -> {
           
            if (saisieMurEnCours) {  //On ne fait les calculs que si la fenêtre "Nouveau Mur" est ouverte
                //Formule mathématique : Convertit les pixels de l'écran en mètres (selon le zoom et le décalage)
                //Math.round(... * 100.0) / 100.0 sert à arrondir à 2 chiffres après la virgule (ex: 2.34)
                double mx = Math.round(((e.getX() - panX) / (50 * zoom)) * 100.0) / 100.0;  //"(50 * zoom)" indique que 50 pixels = 1m
                double my = Math.round(((e.getY() - panY) / (50 * zoom)) * 100.0) / 100.0;

                
                if (attendPremierClic) {   //Si on est en train de placer le premier point du mur
                    //Écrit les coordonnées calculées directement dans les cases "Départ" du formulaire
                    champTx1.setText(String.valueOf(mx));
                    champTy1.setText(String.valueOf(my));
                } else if (!pointArriveeVerrouille) {    //Sinon, si on est en train de placer la fin du mur (et que ce n'est pas figé)
                    //VÉRIFICATION DE FERMETURE (AIMANT) : Si la pièce a déjà au moins un mur
                    if (pieceActuelle != null && pieceActuelle.getMurs()[0] != null) {
                        //Récupère le point exact où la pièce a commencé
                        Coin origine = pieceActuelle.getMurs()[0].getDebut();
                        //Calcule la distance (Théorème de Pythagore) entre la souris et ce point d'origine
                        double dist = Math.sqrt(Math.pow(mx - origine.getCx(), 2) + Math.pow(my - origine.getCy(), 2));
                        
                        if (dist < 0.4) {        // Si on s'approche à moins de 40 cm (0.4m) du point de départ
                            //On force les coordonnées de la souris à être exactement celles du point de départ (Aimantation), 
                            //pour que ça soit plus simple pour l'utilisateur
                            mx = origine.getCx();
                            my = origine.getCy();
                        }
                    }
                    //Écrit les coordonnées dans les cases "Fin" du formulaire
                    champTx2.setText(String.valueOf(mx));
                    champTy2.setText(String.valueOf(my));
                }
            }
        });

        //ÉVÉNEMENT : Que faire quand on CLIQUE sur la zone de dessin ?
        zoneDessin.setOnMouseClicked(e -> {
            if (saisieMurEnCours) { //Ne s'active que si on est en train de tracer un mur
            
                if (attendPremierClic) {     //Si c'était le premier clic (pour le point de départ)
                    //On dit au programme que le départ est fixé, on passe au point d'arrivée
                    attendPremierClic = false;
                    //Le point d'arrivée est libre de suivre la souris
                    pointArriveeVerrouille = false;
                    //Affiche un guide pour l'utilisateur
                    zoneTexte.setText("Départ fixé ! Bougez la souris pour l'arrivée et cliquez pour verrouiller.");
                } else {
                    //Si on reclique, on inverse l'état (Fige ou Libère les coordonnées d'arrivée)
                    pointArriveeVerrouille = !pointArriveeVerrouille;
                    //Si on vient de figer le point
                    if (pointArriveeVerrouille) {
                        //Récupère les coordonnées figées dans les cases de texte
                        double mx = Double.parseDouble(champTx2.getText());
                        double my = Double.parseDouble(champTy2.getText());
                        //Si un mur existe déjà, on vérifie si on vient de fermer la pièce
                        if (pieceActuelle.getMurs()[0] != null) {
                            Coin origine = pieceActuelle.getMurs()[0].getDebut();
                            //Si le point figé est exactement le point de départ de la pièce
                            if (mx == origine.getCx() && my == origine.getCy()) {
                                zoneTexte.setText("Fermeture détectée et verrouillée ! Validez le formulaire.");
                            } else {
                                zoneTexte.setText("Arrivée verrouillée. Validez ou recliquez sur le plan pour modifier.");
                            }
                        } else {
                            zoneTexte.setText("Arrivée verrouillée. Validez ou recliquez sur le plan pour modifier.");
                        }
                    } else {
                        //Si on a déverrouillé, la souris reprend le contrôle
                        zoneTexte.setText("Arrivée déverrouillée. Bougez la souris...");
                    }
                }
            }
        });

        //ÉVÉNEMENT : Quand on MAINTIENT le bouton de la souris (début du glisser)
        zoneDessin.setOnMousePressed(e -> { 
            //Mémorise la position de départ pour calculer le déplacement de la caméra
            lastMouseX = e.getX(); lastMouseY = e.getY(); 
        });
        
        //ÉVÉNEMENT : Quand on GLISSE la souris en maintenant le clic (Panning / Déplacement du plan)
        zoneDessin.setOnMouseDragged(e -> {
            //Sécurité : On interdit de bouger le plan si on est en train de tracer un mur pour éviter les bugs
            if (!saisieMurEnCours) { 
                //Modifie le décalage de la caméra en fonction de la distance parcourue par la souris
                panX += (e.getX() - lastMouseX); panY += (e.getY() - lastMouseY);
                //Met à jour l'ancienne position pour la prochaine fraction de seconde
                lastMouseX = e.getX(); lastMouseY = e.getY();
                //Demande à l'ardoise de se redessiner avec les nouvelles coordonnées de caméra
                actualiserDessin();
            }
        });
        
        //ÉVÉNEMENT : Quand on utilise la MOLETTE de la souris (Zoom)
        zoneDessin.setOnScroll(e -> {
            //Si la molette va vers le haut (valeur positive), on augmente le zoom de 10%
            if (e.getDeltaY() > 0) zoom *= 1.1; 
            //Sinon, on le diminue de 10%
            else zoom /= 1.1;                   
            //Limite de sécurité : Empêche de dézoomer à moins de 20% ou zoomer à plus de 500%
            zoom = Math.max(0.2, Math.min(zoom, 5.0)); 
            //Redessine tout le plan avec la nouvelle échelle
            actualiserDessin();
        });

        //Création de l'arborescence (Explorateur de projet à gauche)
        arbreProjet = new TreeView<>();
        //Ajoute un "espion" qui réagit quand l'utilisateur clique sur un élément de l'arbre
        arbreProjet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            
            //SÉCURITÉ : On bloque le changement de vue si l'utilisateur est en train de tracer une pièce !
            if (pieceEnCours) {
                zoneTexte.setText("⚠️ Impossible de changer de vue : terminez d'abord la pièce en cours !");
                return; //Interrompt la méthode ici, on ne change pas le niveau affiché
            }
            
            //Si on a bien cliqué sur un élément et que le projet est un Immeuble
            if (newValue != null && projetActuel instanceof Immeuble) {
                
                //ASTUCE : Remonter l'arborescence (Dossiers parents)
                TreeItem<String> courant = newValue;
                //Tant que le nom du dossier ne commence pas par "Niveau " (ex: on a cliqué sur un détail de Mur)
                while (courant != null && courant.getValue() != null && !courant.getValue().startsWith("Niveau ")) {
                    courant = courant.getParent(); // On remonte au dossier parent (ex: de Pièce -> Appart -> Niveau)
                }
                
                //Si on a fini par trouver le dossier racine du Niveau
                if (courant != null && courant.getValue().startsWith("Niveau ")) {
                    try {
                        //Découpage propre du texte pour extraire le numéro (ex: "Niveau 1" devient le tableau ["Niveau", "1"])
                        String[] mots = courant.getValue().split(" ");
                        int idN = Integer.parseInt(mots[1]); //Récupère le chiffre 1
                        
                        // Parcourt tous les niveaux de l'immeuble pour trouver celui qui correspond
                        for (Niveau n : ((Immeuble) projetActuel).getNiveaux()) {
                            if (n != null && n.getId() == idN) {
                                niveauAffiche = n; //  On modifie la caméra pour regarder cet étage
                                actualiserDessin(); // On redessine immédiatement le plan
                                break; // On arrête la recherche
                            }
                        }
                    } catch(Exception ex) {} // Ignore silencieusement s'il y a un bug de lecture du texte
                }
            }
        });

        // INSTANCIATION DES BOUTONS AVEC LEURS TEXTES (Emojis intégrés)
        btnNouveau = new Button("Nouveau Projet"); 
        btnAjoutNiveau = new Button("➕ Ajouter un Niveau");
        btnAjoutAppart = new Button("🚪 Ajouter un Appartement"); 
        btnNouvellePiece = new Button("📐 Nouvelle Pièce");
        btnAjouterMur = new Button("➕ Ajouter un Mur"); 
        btnTerminerPiece = new Button("🛑 Forcer Fin Pièce");
        btnCalculer = new Button("Calculer le Devis"); 
        btnVoirCatalogue = new Button("📖 Voir le Catalogue");
        btnExporter = new Button("💾 Exporter le Devis"); 
        btnConsigne = new Button("💡 Consignes d'utilisation");
        
        //Astuce (vue en ligne) : On regroupe tous les boutons dans un tableau pour les configurer tous d'un coup
        Button[] tousLesBoutons = {btnNouveau, btnAjoutNiveau, btnAjoutAppart, btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, btnVoirCatalogue, btnExporter, btnConsigne};
        //Force tous les boutons à s'étirer pour prendre toute la largeur disponible dans leur colonne
        for(Button b : tousLesBoutons) b.setMaxWidth(Double.MAX_VALUE);

        
        //ACTIONS DES BOUTONS (Que se passe-t-il quand on clique dessus ?) +++
        
        //Lancer la méthode d'initialisation de projet
        btnNouveau.setOnAction(e -> initProjet());
        
        //Bouton Ajouter Niveau
        btnAjoutNiveau.setOnAction(e -> {
            //Ne fonctionne que si le projet est un Immeuble
            if (projetActuel instanceof Immeuble) {
                //Remet le compteur d'appartement à zéro puisqu'on commence un nouvel étage
                compteurAppart = 0; 
                //Crée un nouvel objet Niveau (hauteur par défaut 2.50m, 10 apparts max)
                Niveau n = new Niveau(++compteurNiveau, 2.50, 10);
                //Ajoute ce niveau à la liste de l'immeuble (Nécessite de forcer le type / Cast)
                ((Immeuble) projetActuel).ajouterNiveau(n);
                //Définit ce niveau comme celui sur lequel on travaille ET celui qu'on regarde
                niveauActuel = n; niveauAffiche = n; appartActuel = null;
                //Notifie l'utilisateur
                zoneTexte.setText("Niveau " + compteurNiveau + " ajouté ! Ajoutez un appartement.");
                //Met à jour la légende de gauche et vérifie l'état des boutons
                actualiserArbre(); majBoutons();
            }
        });
        
        //Bouton Ajouter Appartement
        btnAjoutAppart.setOnAction(e -> {
            //Vérifie qu'un niveau a bien été sélectionné/créé avant
            if (niveauActuel != null) {
                //On force la caméra à revenir sur l'étage "en chantier"
                //(Au cas où l'utilisateur regardait un vieux niveau dans l'explorateur)
                niveauAffiche = niveauActuel; 
                actualiserDessin();
                //Crée un objet Appartement (20 pièces max)
                Appartement a = new Appartement(++compteurAppart, 20);
                //Ajoute l'appartement au niveau en cours
                niveauActuel.ajouterAppart(a);
                //Définit cet appartement comme la zone de travail actuelle
                appartActuel = a;
                zoneTexte.setText("Appartement " + compteurAppart + " créé ! Créez une pièce maintenant.");
                actualiserArbre(); majBoutons();
            }
        });
        
        //Bouton Nouvelle Pièce
        btnNouvellePiece.setOnAction(e -> {
            //Si c'est un immeuble, on ramène brutalement la vue sur l'étage en cours
            //Cela évite que l'utilisateur dessine sa nouvelle pièce sur le plan du Rez-de-chaussée par erreur !
            if (projetActuel instanceof Immeuble) {
                niveauAffiche = niveauActuel;
                actualiserDessin();
            }
            //Crée une pièce avec un ID unique et un tableau de 50 murs maximum
            pieceActuelle = new Piece(compteurPiece++, 50); 
            //Verrouille l'interface en mode "dessin de pièce"
            pieceEnCours = true; compteurMur = 1; // Réinitialise le compteur de murs pour cette pièce
            zoneTexte.setText("Pièce initialisée. Cliquez sur 'Ajouter un mur' pour dessiner.");
            majBoutons(); //Grise les boutons interdits pendant le dessin
        });
        
        //Bouton Ajouter un Mur (Ouvre le pop-up)
        btnAjouterMur.setOnAction(e -> ouvrirFormulaireMur());
        
        //Bouton Terminer Pièce (Lance le pop-up de fin)
        btnTerminerPiece.setOnAction(e -> finaliserPiece());
        
        //Bouton Calculer le Devis
        btnCalculer.setOnAction(e -> {
            //Appelle la méthode racine métier qui calcule récursivement la somme HT
            double totalHT = projetActuel.devisBatiment();
            //Calcule la TVA (20%)
            double tva = totalHT * 0.20; 
            //Calcule le TTC
            double totalTTC = totalHT + tva;
            //Affiche le ticket de caisse dans la zone de texte (\n = saut de ligne)
            zoneTexte.setText("=== RÉCAPITULATIF DU DEVIS ===\nMontant HT  : " + String.format("%.2f", totalHT) + " €\nTVA (20%)   : " + String.format("%.2f", tva) + " €\nMontant TTC : " + String.format("%.2f", totalTTC) + " €");
        });
        
        //Bouton Voir le Catalogue
        btnVoirCatalogue.setOnAction(e -> {
            //Crée une simple boîte de dialogue (sans valeur de retour 'Void')
            Dialog<Void> d = new Dialog<>(); d.setTitle("Catalogue des Revêtements");
            //Zone de texte pour afficher le catalogue
            TextArea t = new TextArea(); t.setEditable(false);
            //Boucle sur chaque revêtement chargé en mémoire
            for(Revetement r : catalogue) t.appendText("ID " + r.getId() + " - " + r.getDesignation() + " (" + r.getPrix() + " €/m2)\n");
            //Insère le texte dans la boîte et ajoute un bouton Fermer
            d.getDialogPane().setContent(t); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            //Affiche la boîte et bloque le reste jusqu'à ce qu'on la ferme
            d.showAndWait();
        });
        
        //Bouton Consigne (Affiche la doc)
        btnConsigne.setOnAction(e -> afficherConsignes());
        //Bouton Exporter (Génère le .txt)
        btnExporter.setOnAction(e -> exporterDevis());

        
        
        // ---ORGANISATION VISUELLE (LAYOUT)---

        //Création d'une boîte verticale (VBox) pour la légende à gauche, espacement de 5px
        VBox encadreLegende = new VBox(5);
        //Ajout de CSS (Style) pour créer une bordure grise et un fond blanc
        encadreLegende.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 10; -fx-border-radius: 5;");
        Label titreL = new Label("Légende du Plan :"); titreL.setStyle("-fx-font-weight: bold;");
        //Création des labels de couleurs
        Label lMur = new Label("▬ Murs (Trait bleu/rouge)"); lMur.setTextFill(Color.DARKBLUE);
        Label lPorte = new Label("■ Porte (Carré vert)"); lPorte.setTextFill(Color.GREEN);
        Label lFenetre = new Label("● Fenêtre (Rond rouge)"); lFenetre.setTextFill(Color.RED);
        //Ajout des textes dans la boîte de légende
        encadreLegende.getChildren().addAll(titreL, lMur, lPorte, lFenetre);

        //Création de la grande colonne de GAUCHE (VBox, espacement 10px)
        VBox menuGauche = new VBox(10);
        menuGauche.setPadding(new Insets(10)); // +++ Marges intérieures de 10px
        menuGauche.setPrefWidth(220); // +++ Largeur imposée
        Label lblExplo = new Label("Explorateur :"); lblExplo.setStyle("-fx-font-weight: bold;");
        // Force le composant "arbreProjet" à s'étirer vers le bas pour prendre l'espace vide
        VBox.setVgrow(arbreProjet, Priority.ALWAYS); 
        //Ajout de la légende et de l'arbre dans la colonne gauche
        menuGauche.getChildren().addAll(encadreLegende, lblExplo, arbreProjet);

        //Création de la grande colonne de DROITE pour les boutons
        VBox menuDroite = new VBox(10);
        menuDroite.setPadding(new Insets(10)); 
        menuDroite.setPrefWidth(220);
        //Empilement de tous les boutons et de la zone de notification
        menuDroite.getChildren().addAll(
            new Label("Actions :"), btnNouveau, btnAjoutNiveau, btnAjoutAppart, 
            btnNouvellePiece, btnAjouterMur, btnTerminerPiece, btnCalculer, 
            btnVoirCatalogue, btnExporter, btnConsigne, 
            new Label("Informations :"), zoneTexte
        );

        //Découpage final de l'écran en 5 zones (BorderPane)
        BorderPane principal = new BorderPane();
        principal.setLeft(menuGauche);   //Colonne gauche
        principal.setCenter(zoneDessin); //Le plan au centre (prend le plus de place)
        principal.setRight(menuDroite);  //Colonne droite avec boutons

        //Active ou désactive les boutons logiquement avant l'affichage final
        majBoutons(); 

        //Création de la "Scène" (le contenu de la fenêtre) avec une taille fixe de 1250x650
        primaryStage.setScene(new Scene(principal, 1250, 650));
        //Ordre d'afficher la fenêtre à l'écran
        primaryStage.show();
    }
    
    
    
    
    //      ---METHODES---
    
    
    
    // MÉTHODE : Création du formulaire de base (Maison ou Immeuble)
    private void initProjet() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paramètres du Nouveau Projet");
        // Associe ce pop-up à la fenêtre principale (empêche de passer en arrière-plan)
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        //Champ de texte par défaut
        TextField txtNom = new TextField("MonProjet");
        //Menu déroulant
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Maison (Plain-pied)", "Immeuble / Étage");
        cbType.getSelectionModel().selectFirst(); // +++ Sélectionne Maison par défaut
        
        //Grille pour aligner les textes et les cases
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        //Colonne 0 = Label, Colonne 1 = Champ de saisie
        grid.add(new Label("Nom du projet :"), 0, 0); grid.add(txtNom, 1, 0);
        grid.add(new Label("Type de bâtiment :"), 0, 1); grid.add(cbType, 1, 1);
        
        //Intègre la grille dans la fenêtre et ajoute les boutons standards OK/Annuler
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        //Affiche la fenêtre et attend la réponse de l'utilisateur
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) { // Si on a cliqué sur OK
                //Réinitialisation totale (Remise à zéro des compteurs et états)
                compteurPiece = 1; compteurNiveau = 0; compteurAppart = 0;
                pieceEnCours = false; pieceActuelle = null; appartActuel = null;
                
                // Si l'index 0 (Maison) est sélectionné
                if (cbType.getSelectionModel().getSelectedIndex() == 0) {
                    projetActuel = new Maison(txtNom.getText(), 100); // 100 pièces max
                    niveauActuel = null; niveauAffiche = null; // Pas de niveaux dans une maison
                    zoneTexte.setText("Projet Maison créé. Ajoutez directement une pièce !");
                } else {
                    //Si c'est un immeuble
                    projetActuel = new Immeuble(txtNom.getText(), 50); // 50 niveaux max
                    //Création automatique du RDC
                    Niveau n0 = new Niveau(0, 2.50, 10); 
                    ((Immeuble) projetActuel).ajouterNiveau(n0);
                    niveauActuel = n0; niveauAffiche = n0;
                    zoneTexte.setText("Projet Immeuble créé. RDC configuré (Niveau 0). Ajoutez un appartement.");
                }
                //Mise à jour visuelle globale
                actualiserDessin(); actualiserArbre(); majBoutons();
            }
        });
    }

    //MÉTHODE : Formulaire flottant pour tracer un Mur
    private void ouvrirFormulaireMur() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Mur");
        // Modality.NONE permet de cliquer sur le plan PENDANT QUE la fenêtre est ouverte (fenêtre flottante)
        dialog.initModality(javafx.stage.Modality.NONE);
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        //Recherche si un mur a déjà été tracé dans la pièce actuelle
        Mur dernierMur = null;
        for (Mur m : pieceActuelle.getMurs()) if (m != null) dernierMur = m;
        
        //Création des 4 cases pour les coordonnées X et Y
        TextField tx1 = new TextField(), ty1 = new TextField(), tx2 = new TextField(), ty2 = new TextField();
        
        //Si un mur existe déjà (Ce n'est pas le 1er mur)
        if (dernierMur != null) {
            //Le point de départ (1) est automatiquement la fin (2) du mur précédent
            tx1.setText(String.valueOf(dernierMur.getFin().getCx())); 
            ty1.setText(String.valueOf(dernierMur.getFin().getCy()));
            //On empêche l'utilisateur de modifier ces cases (sécurité de chaînage)
            tx1.setEditable(false); ty1.setEditable(false);
            //On grise les cases visuellement
            tx1.setStyle("-fx-background-color: #eeeeee;"); ty1.setStyle("-fx-background-color: #eeeeee;");
            attendPremierClic = false; //Le départ est déjà connu, on attend l'arrivée
            zoneTexte.setText("Bougez la souris pour placer l'arrivée du mur, puis cliquez pour verrouiller.");
        } else {
            //Si c'est le 1er mur, tout est vide et on attend le clic de départ
            attendPremierClic = true; 
            zoneTexte.setText("1er mur : Bougez la souris et cliquez pour fixer le DÉPART.");
        }

        //On connecte les cases de la fenêtre aux variables globales pour que la souris puisse les remplir
        champTx1 = tx1; champTy1 = ty1; champTx2 = tx2; champTy2 = ty2;
        //On indique au programme qu'on est en mode saisie
        saisieMurEnCours = true; pointArriveeVerrouille = false;
        //On désactive le bouton pour éviter d'ouvrir 2 fenêtres en même temps
        btnAjouterMur.setDisable(true); 

        //Cases pour le nombre de portes et fenêtres
        TextField tp = new TextField("0"), tf = new TextField("0");
        //Menu déroulant pour le revêtement du mur
        ComboBox<String> cb = new ComboBox<>(); cb.getItems().add("Aucun");
        //Filtre le catalogue : Ne propose que les revêtements adaptés pour les MURS
        for (Revetement r : catalogue) if (r.estPourMur()) cb.getItems().add(r.getDesignation());
        cb.getSelectionModel().selectFirst();
        
        //Grille de mise en page du formulaire
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Début X, Y (m):"), 0, 0); g.add(tx1, 1, 0); g.add(ty1, 2, 0);
        g.add(new Label("Fin X, Y (m):"), 0, 1); g.add(tx2, 1, 1); g.add(ty2, 2, 1);
        g.add(new Label("Nombre de Portes / Fenêtres :"), 0, 2); g.add(tp, 1, 2); g.add(tf, 2, 2);
        g.add(new Label("Revêtement mural :"), 0, 3); g.add(cb, 1, 3, 2, 1);
        dialog.getDialogPane().setContent(g); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        //Récupération du bouton OK interne de JavaFX pour pouvoir le bloquer
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setDisable(true); //Désactivé par défaut
        
        //ROUTINE (Listener) : Vérifie en permanence si une des 4 cases de coordonnées est vide
        Runnable verif = () -> btnOk.setDisable(tx2.getText().isEmpty() || ty2.getText().isEmpty() || tx1.getText().isEmpty() || ty1.getText().isEmpty());
        //Ajoute un "espion" sur les 4 cases qui exécute la vérification à chaque touche tapée
        tx1.textProperty().addListener((o, old, n) -> verif.run()); ty1.textProperty().addListener((o, old, n) -> verif.run());
        tx2.textProperty().addListener((o, old, n) -> verif.run()); ty2.textProperty().addListener((o, old, n) -> verif.run());

        //Affiche la fenêtre flottante
        dialog.showAndWait().ifPresent(r -> {
            saisieMurEnCours = false; //Fin de la saisie
            if (r == ButtonType.OK) { //Si validé
                try {
                    // CRÉATION OBJET MÉTIER : Un Mur, contenant un Coin départ et un Coin fin (ID 1 et 2), capacité 20 ouvertures
                    Mur m = new Mur(compteurMur++, new Coin(1, Double.parseDouble(tx1.getText()), Double.parseDouble(ty1.getText())), 
                                   new Coin(2, Double.parseDouble(tx2.getText()), Double.parseDouble(ty2.getText())), 20);
                    
                    //POLYMORPHISME : Ajout des Portes et des Fenêtres (qui héritent toutes les deux de 'Ouverture')
                    for (int i=0; i<Integer.parseInt(tp.getText()); i++) m.ajouterOuverture(new Porte(i));
                    for (int i=0; i<Integer.parseInt(tf.getText()); i++) m.ajouterOuverture(new Fenetre(i));
                    
                    //Application du revêtement s'il n'est pas "Aucun"
                    if (!cb.getValue().equals("Aucun")) {
                        //Cherche l'objet Revêtement correspondant au texte dans le catalogue
                        for (Revetement rev : catalogue) if (cb.getValue().equals(rev.getDesignation())) m.appliquerRevetement(rev);
                    }
                    
                    // Ajout du mur construit à la pièce
                    pieceActuelle.ajouterMur(m);
                    
                    //VÉRIFICATION AUTO-FERMETURE : Si on a au moins 3 murs et que l'arrivée touche le tout premier départ
                    Mur pre = pieceActuelle.getMurs()[0];
                    if (compteurMur > 3 && m.getFin().getCx() == pre.getDebut().getCx() && m.getFin().getCy() == pre.getDebut().getCy()) {
                        actualiserDessin(); actualiserArbre(); 
                        finaliserPiece(); // +++ On ferme la pièce automatiquement
                    } else {
                        //Sinon, on met juste à jour l'écran
                        actualiserDessin(); actualiserArbre();
                    }
                } catch (Exception ex) { zoneTexte.setText("Erreur dans les formats numériques."); } // +++ Sécurité si texte saisi au lieu d'un chiffre
            }
            majBoutons(); //Réactive le bouton Ajouter Mur
        });
        saisieMurEnCours = false; //Double sécurité si fermeture forcée
        majBoutons();
    }

    //MÉTHODE : Fin de la pièce et choix du sol/plafond
    private void finaliserPiece() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Fin de la pièce - Choix des surfaces horizontales");
        dialog.initOwner(zoneDessin.getScene().getWindow());
        
        //Zone de texte pour le nom personnalisé (prérempli avec "Pièce n°...")
        TextField txtNomPiece = new TextField("Pièce n°" + (compteurPiece-1));
        ComboBox<String> cbSol = new ComboBox<>(), cbPlat = new ComboBox<>();
        cbSol.getItems().add("Aucun"); cbPlat.getItems().add("Aucun");
        
        //Filtrage des revêtements adaptés pour Sol ou Plafond
        for(Revetement r : catalogue) {
            if(r.estPourSol()) cbSol.getItems().add(r.getDesignation());
            if(r.estPourPlafond()) cbPlat.getItems().add(r.getDesignation());
        }
        cbSol.getSelectionModel().selectFirst(); cbPlat.getSelectionModel().selectFirst();
        
        //Mise en page de la fenêtre
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(20));
        g.add(new Label("Nommer la pièce :"), 0, 0); g.add(txtNomPiece, 1, 0);
        g.add(new Label("Revêtement Sol :"), 0, 1); g.add(cbSol, 1, 1);
        g.add(new Label("Revêtement Plafond :"), 0, 2); g.add(cbPlat, 1, 2);
        dialog.getDialogPane().setContent(g); 
        
        //Boutton pour recommencer
        //Le paramètre CANCEL_CLOSE permet de fermer la fenêtre sans déclencher l'action par défaut
        ButtonType btnRecommencer = new ButtonType("🔄 Recommencer", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        //On ajoute le bouton de validation (OK) ET notre nouveau bouton au pop-up
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, btnRecommencer);
        
        //Affiche la fenêtre et attend que l'utilisateur clique sur l'un des boutons
        dialog.showAndWait().ifPresent(r -> {
            
            //CAS 1 : Si l'utilisateur clique sur "OK" (Il valide sa pièce pour de bon)
            if (r == ButtonType.OK) {
                //On donne le nom personnalisé tapé par l'utilisateur à l'objet Pièce
                pieceActuelle.setNom(txtNomPiece.getText());
                
                //Application des objets revêtements au Sol et au Plafond
                for(Revetement rev : catalogue) {
                    if(cbSol.getValue().equals(rev.getDesignation())) pieceActuelle.getSol().appliquerRevetement(rev);
                    if(cbPlat.getValue().equals(rev.getDesignation())) pieceActuelle.getPlafond().appliquerRevetement(rev);
                }
                
                //Rangement de la pièce au bon endroit dans l'architecture (Maison ou Immeuble)
                if (projetActuel instanceof Maison) {
                    ((Maison) projetActuel).ajouterPiece(pieceActuelle);
                } else if (appartActuel != null) {
                    appartActuel.ajouterPiece(pieceActuelle);
                }
                
                //On indique que la pièce est terminée (déverrouille l'interface)
                pieceEnCours = false; pieceActuelle = null;
                zoneTexte.setText("Pièce enregistrée avec succès !");
                actualiserDessin(); actualiserArbre(); majBoutons();
            } 
            
            //CAS 2 : Si l'utilisateur clique sur notre bouton "Recommencer"
            else if (r == btnRecommencer) {
                //On écrase la pièce actuelle par une nouvelle pièce totalement vide
                //ASTUCE : On utilise (compteurPiece - 1) pour qu'elle conserve son numéro d'ID d'origine
                pieceActuelle = new Piece(compteurPiece - 1, 50); 
                
                //On remet le compteur interne de murs à 1
                compteurMur = 1; 
                
                //On notifie l'utilisateur que le reset a fonctionné
                zoneTexte.setText("Pièce réinitialisée. Cliquez sur 'Ajouter un mur' pour recommencer à zéro.");
                
                //On force le plan à se redessiner : comme la pièce est vide, les anciens murs bleus vont disparaître
                actualiserDessin(); 
                
                //On met à jour l'état des boutons (le bouton "Ajouter un mur" sera à nouveau cliquable)
                majBoutons();
            }
        });
    }

    //MÉTHODE : Moteur de rendu graphique, dessine le pan
    private void actualiserDessin() {
        //Récupération de l'outil Pinceau (GraphicsContext) de JavaFX
        GraphicsContext gc = zoneDessin.getGraphicsContext2D();
        //Efface la totalité de l'ardoise (évite la superposition des traits)
        gc.clearRect(0, 0, zoneDessin.getWidth(), zoneDessin.getHeight());
        
        //TRACÉ DE LA GRILLE DE FOND (Lignes grises)
        gc.setStroke(Color.web("#e0e0e0")); gc.setLineWidth(0.5); //Gris clair, trait très fin
        double tailleCase = 50 * zoom; //Une case fait 1 mètre (50 pixels par défaut), adaptée au zoom
        //Boucles pour tracer les lignes verticales et horizontales avec décalage de la caméra
        for (double x = panX % tailleCase; x < zoneDessin.getWidth(); x += tailleCase) gc.strokeLine(x, 0, x, zoneDessin.getHeight());
        for (double y = panY % tailleCase; y < zoneDessin.getHeight(); y += tailleCase) gc.strokeLine(0, y, zoneDessin.getWidth(), y);
        
        //TRACÉ DES AXES PRINCIPAUX (Le point 0,0 en mètre)
        gc.setStroke(Color.web("#b0b0b0")); gc.setLineWidth(1.5); // +++ Gris plus foncé et plus épais
        gc.strokeLine(panX, 0, panX, zoneDessin.getHeight());
        gc.strokeLine(0, panY, zoneDessin.getWidth(), panY);
        
        //Si aucun projet n'est créé, on s'arrête là (plan vide)
        if (projetActuel == null) return;
        
        //TRACÉ DES MURS (Épaisseur 3 pixels)
        gc.setLineWidth(3.0);
        
        //Si c'est une Maison (Tous les murs sont Bleu foncé)
        if (projetActuel instanceof Maison) {
            gc.setStroke(Color.DARKBLUE);
            for (Piece p : ((Maison) projetActuel).getPieces()) if (p != null) dessinerMursPiece(gc, p);
        } 
        //Si c'est un Immeuble (On ne dessine QUE l'étage affiché 'niveauAffiche', murs Rouge foncé)
        else if (niveauAffiche != null) {
            gc.setStroke(Color.DARKRED);
            for (Appartement app : niveauAffiche.getApparts()) {
                if (app != null) {
                    for (Piece p : app.getPieces()) if (p != null) dessinerMursPiece(gc, p);
                }
            }
        }
        
        //Si on est en train de tracer une nouvelle pièce, on dessine ses premiers murs en Bleu clair
        if (pieceEnCours && pieceActuelle != null) {
            gc.setStroke(Color.DEEPSKYBLUE); gc.setLineWidth(4.0); //Trait plus épais pour ressortir
            dessinerMursPiece(gc, pieceActuelle);
        }
    }

    //SOUS-MÉTHODE DE DESSIN : Trace les détails internes d'une pièce
    private void dessinerMursPiece(GraphicsContext gc, Piece p) {
        //Variables pour calculer le "Centre de Gravité" de la pièce (Moyenne des coordonnées)
        double sommeX = 0, sommeY = 0;
        int nbPointsPourCentre = 0;

        //Parcourt tous les murs de la pièce
        for (Mur m : p.getMurs()) {
            if (m != null) {
                //Conversion mathématique (Mètres réels -> Pixels écran) prenant en compte Zoom et Caméra
                double x1 = m.getDebut().getCx() * (50 * zoom) + panX;
                double y1 = m.getDebut().getCy() * (50 * zoom) + panY;
                double x2 = m.getFin().getCx() * (50 * zoom) + panX;
                double y2 = m.getFin().getCy() * (50 * zoom) + panY;
                
                //Trace la ligne principale du mur
                gc.strokeLine(x1, y1, x2, y2);
                
                //Dessine un petit point noir sur les angles (Jointures)
                gc.setFill(Color.BLACK);
                gc.fillOval(x1-3, y1-3, 6, 6); 
                
                //Ajoute les coordonnées pour calculer le centre plus tard
                sommeX += x1;
                sommeY += y1;
                nbPointsPourCentre++;

                //COMPTAGE DES OUVERTURES (Analyse via instanceof)
                int nbPortes = 0;
                int nbFenetres = 0;
                for (int i = 0; i < m.getNbO(); i++) {
                    if (m.getOuvertures()[i] instanceof Porte) nbPortes++;
                    if (m.getOuvertures()[i] instanceof Fenetre) nbFenetres++;
                }

                int totalOuvertures = nbPortes + nbFenetres;
                //S'il y a des ouvertures à dessiner
                if (totalOuvertures > 0) {
                    //alcul de proportion : Coupe le mur en segments égaux pour répartir les ouvertures
                    double espacementX = (x2 - x1) / (totalOuvertures + 1);
                    double espacementY = (y2 - y1) / (totalOuvertures + 1);
                    int ouvertureCourante = 1; //Multiplicateur de position
                    
                    //DESSIN DES PORTES (Carré Vert)
                    gc.setFill(Color.GREEN);
                    for(int i = 0; i < nbPortes; i++) {
                        //Calcule la position X/Y exacte sur le segment du mur
                        double ox = x1 + espacementX * ouvertureCourante;
                        double oy = y1 + espacementY * ouvertureCourante;
                        //Dessine un carré de 10x10 centré sur le point
                        gc.fillRect(ox - 5, oy - 5, 10, 10);
                        ouvertureCourante++; //Passe à l'emplacement suivant
                    }
                    
                    //DESSIN DES FENÊTRES (Cercle Rouge)
                    gc.setFill(Color.RED); 
                    for(int i = 0; i < nbFenetres; i++) {
                        double ox = x1 + espacementX * ouvertureCourante;
                        double oy = y1 + espacementY * ouvertureCourante;
                        //Dessine un cercle de 10x10 centré sur le point
                        gc.fillOval(ox - 5, oy - 5, 10, 10);
                        ouvertureCourante++;
                    }
                    
                    //SÉCURITÉ : Remet la bonne couleur de pinceau pour les murs suivants (car on l'a changé en Rouge/Vert)
                    if (projetActuel instanceof Maison) gc.setStroke(Color.DARKBLUE);
                    else gc.setStroke(Color.DARKRED);
                    if (pieceEnCours && p == pieceActuelle) gc.setStroke(Color.DEEPSKYBLUE);
                }
            }
        }

        //DESSIN DU NOM PERSONNALISÉ AU CENTRE DE LA PIÈCE
        //Ne s'affiche que si la pièce est fermée (plus de 2 points) et qu'on a terminé de la tracer
        if (p != pieceActuelle) { 
            //Fait la moyenne de toutes les coordonnées pour trouver le milieu exact
            double centreX = sommeX / nbPointsPourCentre;
            double centreY = sommeY / nbPointsPourCentre;
            
            //Configure le texte (Couleur Noir, Police Arial, Gras, et taille dynamique selon le Zoom)
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14 * zoom)); 
            //Force le point d'origine du texte à être parfaitement centré (et non aligné à gauche)
            gc.setTextAlign(TextAlignment.CENTER); 
            //Imprime le vrai nom de la pièce sur le plan
            gc.fillText(p.getNom(), centreX, centreY); 
        }
    }

    //MÉTHODE : Met à jour l'arborescence (Dossiers à gauche)
    private void actualiserArbre() {
        //Vide l'arbre si aucun projet n'existe en mémoire
        if (projetActuel == null) { arbreProjet.setRoot(null); return; }
        
        //Crée le dossier Racine avec le nom du projet en majuscules
        TreeItem<String> racine = new TreeItem<>(projetActuel.getId().toUpperCase());
        //Force le dossier principal à être déroulé (ouvert) par défaut
        racine.setExpanded(true); 
        
        //LOGIQUE MAISON : On insère directement les pièces à la racine
        if (projetActuel instanceof Maison) {
            for (Piece p : ((Maison) projetActuel).getPieces()) {
                if (p != null) {
                    racine.getChildren().add(genererNoeudPiece(p)); 
                }
            }
        } 
        //LOGIQUE IMMEUBLE : Imbrication Niveaux > Appartements > Pièces
        else {
            for (Niveau n : ((Immeuble) projetActuel).getNiveaux()) {
                if (n == null) continue; //Saute les emplacements vides
                TreeItem<String> itemN = new TreeItem<>("Niveau " + n.getId());
                for (Appartement app : n.getApparts()) {
                    if (app == null) continue;
                    TreeItem<String> itemA = new TreeItem<>("Appartement " + app.getId());
                    for (Piece p : app.getPieces()) {
                        if (p != null) {
                            itemA.getChildren().add(genererNoeudPiece(p)); 
                        }
                    }
                    itemN.getChildren().add(itemA); //Ajoute l'appart dans le niveau
                }
                racine.getChildren().add(itemN); // Ajoute le niveau dans la racine
            }
        }
        //Applique la nouvelle structure calculée au composant visuel de l'interface
        arbreProjet.setRoot(racine);
    }

    //NOUVELLE SOUS-MÉTHODE : Générateur de détails pour l'arbre
    //Prend une Pièce en paramètre et retourne un "Dossier" (TreeItem) bien rempli
    private TreeItem<String> genererNoeudPiece(Piece p) {
        //Crée le dossier parent avec le nom de la pièce ET sa superficie formattée à 2 chiffres
        TreeItem<String> noeudPiece = new TreeItem<>(p.getNom() + " (" + String.format("%.2f", p.surfaceSol()) + " m2)");
        
        //ANALYSE DU SOL
        //Condition ternaire (Si le revêtement n'est pas null, on prend son nom, sinon on écrit "Aucun")
        String revSol = (p.getSol().getRevetement() != null) ? p.getSol().getRevetement().getDesignation() : "Aucun";
        //Ajoute la ligne du sol dans le dossier de la pièce
        noeudPiece.getChildren().add(new TreeItem<>("🟫 Sol : " + revSol));
        
        //ANALYSE DU PLAFOND
        String revPlat = (p.getPlafond().getRevetement() != null) ? p.getPlafond().getRevetement().getDesignation() : "Aucun";
        //Ajoute la ligne du plafond
        noeudPiece.getChildren().add(new TreeItem<>("⬜ Plafond : " + revPlat));
        
        //ANALYSE DES MURS
        //Crée un sous-dossier spécial pour éviter de saturer la vue si la pièce a 10 murs
        TreeItem<String> noeudMurs = new TreeItem<>("🧱 Détails des Murs");
        
        //Boucle sur le tableau des murs de la pièce
        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i]; //Récupère le mur actuel
            if (m != null) { //Sécurité si le mur existe
                //Vérifie le revêtement de ce mur précis
                String revMur = (m.getRevetement() != null) ? m.getRevetement().getDesignation() : "Aucun";
                
                //CONSTRUCTION DE LA PHRASE D'INFORMATION DU MUR
                //Concatène la longueur, les coordonnées (X,Y) du Point de Départ (Debut) et d'Arrivée (Fin)
                String infoMur = "Mur " + (i+1) + " : " + String.format("%.2f", m.longueur()) + "m " + 
                                 "| de [" + m.getDebut().getCx() + " ; " + m.getDebut().getCy() + "] " +
                                 "à [" + m.getFin().getCx() + " ; " + m.getFin().getCy() + "] " +
                                 "| Rev: " + revMur;
                
                //Ajoute cette grande ligne de détail dans le sous-dossier des murs
                noeudMurs.getChildren().add(new TreeItem<>(infoMur));
            }
        }
        
        //Emboîte le sous-dossier des murs dans le dossier global de la pièce
        noeudPiece.getChildren().add(noeudMurs);
        
        //Retourne la pièce "packagée" et prête à l'emploi
        return noeudPiece;
    }

    //MÉTHODE : Système de Sécurité (Grisage des boutons selon le contexte)
    private void majBoutons() {
        boolean aProjet = (projetActuel != null);
        boolean estImmeuble = aProjet && (projetActuel instanceof Immeuble);
        
        //On ne peut pas ajouter de niveau si ce n'est pas un immeuble, ou si on est en train de tracer une pièce
        btnAjoutNiveau.setDisable(!estImmeuble || pieceEnCours);
        btnAjoutAppart.setDisable(!estImmeuble || niveauActuel == null || pieceEnCours);
        
        if (estImmeuble) btnNouvellePiece.setDisable(appartActuel == null || pieceEnCours);
        else btnNouvellePiece.setDisable(!aProjet || pieceEnCours);
        
        btnAjouterMur.setDisable(!pieceEnCours || saisieMurEnCours);
        //On ne peut forcer la fin d'une pièce que si on a au moins 3 murs pour faire un triangle
        btnTerminerPiece.setDisable(!pieceEnCours || compteurMur < 3);
        
        btnCalculer.setDisable(!aProjet || pieceEnCours);
        btnVoirCatalogue.setDisable(pieceEnCours);
        btnExporter.setDisable(!aProjet || pieceEnCours);
        btnConsigne.setDisable(pieceEnCours);
    }

    //MÉTHODE : Lit le fichier texte pour créer les objets Revêtement en mémoire
    private void chargerCatalogue(String f) {
        //Utilisation d'un BufferedReader pour lire le fichier ligne par ligne (Bloc Try-With-Resources)
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); //Lit (et ignore) la première ligne qui contient les en-têtes des colonnes
            String l;
            // Boucle tant qu'il y a des lignes à lire
            while ((l = br.readLine()) != null) {
                //Découpe la ligne à chaque point-virgule (Création d'un tableau d'informations)
                String[] t = l.split(";");
                int id = Integer.parseInt(t[0]); // ID
                double p = Double.parseDouble(t[5]); // Prix
                // Création d'un objet Peinture (ou Carrelage) avec analyse des booléens "1" ou "0"
                catalogue.add(new Peinture(id, t[1], p, t[2].equals("1"), t[3].equals("1"), t[4].equals("1")));
            }
        // Si le fichier est introuvable (Erreur d'exception capturée)
        } catch (Exception e) { zoneTexte.setText("Attention: fichier catalogue non trouvé."); }
    }

    // MÉTHODE : Création du fichier de Devis final
    private void exporterDevis() {
        //Outil JavaFX pour ouvrir la fenêtre "Enregistrer Sous..." de l'ordinateur
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le Devis Détaillé");
        //Force le format texte
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichier Texte", "*.txt"));
        //Propose un nom de fichier par défaut
        fileChooser.setInitialFileName("ResultatDevis_" + projetActuel.getId() + ".txt");
        
        java.io.File file = fileChooser.showSaveDialog(null);
        if (file != null) { //Si l'utilisateur n'a pas annulé
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                //Écriture de l'en-tête du document
                pw.println("===========================================================");
                pw.println("             DEVIS DÉTAILLÉ - " + projetActuel.getId().toUpperCase());
                pw.println("             Généré le : " + new java.util.Date()); //Ajout de la date système
                pw.println("===========================================================");
                
                // Calcul global via la racine du projet
                double totalHT = projetActuel.devisBatiment();
                double tva = totalHT * 0.20;
                double totalTTC = totalHT + tva;

                // Impression des totaux au format monétaire (2 chiffres après la virgule)
                pw.println("SURFACE SOL TOTALE : " + String.format("%.2f", projetActuel.surfaceSolBatiment()) + " m2");
                pw.println("MONTANT TOTAL HT   : " + String.format("%.2f", totalHT) + " €");
                pw.println("TVA (20%)          : " + String.format("%.2f", tva) + " €");
                pw.println("MONTANT TOTAL TTC  : " + String.format("%.2f", totalTTC) + " €");
                pw.println("===========================================================");
                
                //Parcours de l'arborescence pour détailler les pièces (Polymorphisme)
                if (projetActuel instanceof Maison) {
                    // Hauteur par défaut 2.5m pour une maison
                    for (Piece p : ((Maison)projetActuel).getPieces()) if (p != null) ecrirePiece(pw, p, 2.5); 
                } else {
                    for (Niveau n : ((Immeuble)projetActuel).getNiveaux()) {
                        if (n == null) continue;
                        pw.println("\n>>> NIVEAU " + n.getId() + " [HSP : " + n.getHauteur() + "m]");
                        for (Appartement app : n.getApparts()) {
                            if (app == null) continue;
                            pw.println("    > APPARTEMENT " + app.getId());
                            //Transfert de la hauteur de plafond spécifique au niveau
                            for (Piece p : app.getPieces()) if (p != null) ecrirePiece(pw, p, n.getHauteur());
                        }
                    }
                }
                zoneTexte.setText("Devis détaillé exporté avec succès !");
            //Si erreur de droit d'écriture ou disque plein, on alerte sans crasher
            } catch (Exception e) { zoneTexte.setText("Erreur lors de l'exportation du fichier."); }
        }
    }

    //SOUS-MÉTHODE : Détail du calcul pour UNE pièce spécifique
    private void ecrirePiece(java.io.PrintWriter pw, Piece p, double hauteur) {
        //Écrit le nom personnalisé de la pièce en majuscules
        pw.println("\n      " + p.getNom().toUpperCase() + " (Surface Sol Brut : " + String.format("%.2f", p.surfaceSol()) + " m2)");
        
        //Traitement du Sol
        if (p.getSol().getRevetement() != null) {
            double surfaceSolNette = p.surfaceSol();
            //Déduction de la surface des trémies (trous) si elles existent
            if (p.getSol().getNbT() > 0) surfaceSolNette = p.getSol().surfaceNette(p.surfaceSol());
            // Calcul financier
            double prixS = p.getSol().getRevetement().montant(surfaceSolNette);
            pw.println("        - Revêtement Sol : " + p.getSol().getRevetement().getDesignation() + " | Coût : " + String.format("%.2f", prixS) + " €");
        }
        
        // Traitement du Plafond (Similaire au sol)
        if (p.getPlafond().getRevetement() != null) {
            double prixP = p.getPlafond().getRevetement().montant(p.surfaceSol());
            pw.println("        - Revêtement Plafond : " + p.getPlafond().getRevetement().getDesignation() + " | Coût : " + String.format("%.2f", prixP) + " €");
        }
        
        // Note d'information pour justifier l'écart de surface si trémies
        if (p.getSol().getNbT() > 0) pw.println("        - Note : Surface nette sol après trémies : " + String.format("%.2f", p.getSol().surfaceNette(p.surfaceSol())) + " m2");

        pw.println("        - Murs :");
        //Boucle sur chaque mur (Traitement individuel)
        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i];
            if (m != null) {
                // Surface brute = L * H
                double surfaceBrute = m.longueur() * hauteur;
                double surfaceOuvertures = 0;
                // Additionne les surfaces de chaque Porte et Fenêtre (Polymorphisme sur .surface())
                for (int j = 0; j < m.getNbO(); j++) if (m.getOuvertures()[j] != null) surfaceOuvertures += m.getOuvertures()[j].surface();
                // La surface nette ne peut pas être négative (Math.max)
                double surfaceNette = Math.max(0, surfaceBrute - surfaceOuvertures);
                
                // Impression des détails du mur
                pw.print("          * Mur " + (i+1) + " : Long: " + String.format("%.2f", m.longueur()) + "m | Surf brute: " + String.format("%.2f", surfaceBrute) + " m2");
                if (surfaceOuvertures > 0) pw.print(" | Ouvertures: -" + String.format("%.2f", surfaceOuvertures) + " m2 | Surf nette: " + String.format("%.2f", surfaceNette) + " m2");
                if (m.getRevetement() != null) pw.print(" | Revêtement : " + m.getRevetement().getDesignation() + " (" + String.format("%.2f", m.getRevetement().montant(surfaceNette)) + " €)");
                else pw.print(" | Aucun revêtement");
                pw.println(); // Retour à la ligne
            }
        }
        //Total global de cette pièce seule
        pw.println("        >> TOTAL POUR CETTE PIÈCE : " + String.format("%.2f", p.devisPiece(hauteur)) + " €");
    }

    // MÉTHODE : Affiche le mode d'emploi du logiciel
    private void afficherConsignes() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Consignes d'utilisation"); dialog.initOwner(zoneDessin.getScene().getWindow());
        // Récupère le texte depuis la classe statique Consigne
        TextArea txtConsigne = new TextArea(Consigne.getTexte());
        txtConsigne.setEditable(false); txtConsigne.setWrapText(true); txtConsigne.setPrefSize(550, 450);
        txtConsigne.setStyle("-fx-font-size: 14px;");
        dialog.getDialogPane().setContent(txtConsigne); dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    public static void main(String[] args) { launch(args); } //Appel natif pour démarrer le moteur JavaFX
}