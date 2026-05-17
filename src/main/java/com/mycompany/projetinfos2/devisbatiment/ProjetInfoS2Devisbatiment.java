/* =========================================================================
 * NOM DE LA CLASSE : ProjetInfoS2Devisbatiment
 * DATE DE MISE À JOUR : Mai 2026
 * CATÉGORIE TECHNIQUE : Point d'entrée du programme (Méthode Main - Console)
 * =========================================================================
 * DESCRIPTION EN 2 LIGNES :
 * Ancienne classe principale (version texte). Elle charge le catalogue,
 * lance l'interrogatoire dans la console et sauvegarde le devis en .txt.
 * =========================================================================
 * @author Clémentine / Siméon
 * =========================================================================
 */
package com.mycompany.projetinfos2.devisbatiment;

import java.io.*;
import java.util.Date;

public class ProjetInfoS2Devisbatiment {
    // Tableau fixe pour charger le catalogue en mémoire vive
    private static Revetement[] catalogue = new Revetement[200];
    private static int nbRev = 0;

    // Méthode de lancement pour la version purement texte du programme
    public static void main(String[] args) {
        chargerCatalogue("CatalogueRevetements.txt");
        GestionDemande gd = new GestionDemande(catalogue, nbRev);
        Batiment bat = gd.creerBatiment();
        
        double total = bat.devisBatiment();
        System.out.println("\nDevis Total : " + total + " Euros");
        
        sauvegarder(bat, total);
    }

    // Lecture du fichier catalogue pour instancier les objets Revetement
    private static void chargerCatalogue(String f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // On saute la première ligne d'en-tête du fichier CSV/texte
            String l;
            while ((l = br.readLine()) != null) {
                String[] t = l.split(";");
                int id = Integer.parseInt(t[0]);
                double p = Double.parseDouble(t[5]);
                // On peuple notre catalogue avec des objets Peinture (gestion par polymorphisme)
                catalogue[nbRev++] = new Peinture(id, t[1], p, t[2].equals("1"), t[3].equals("1"), t[4].equals("1"));
            }
        } catch (Exception e) { 
            System.out.println("Erreur lors du chargement du catalogue."); 
        }
    }

    // Exporte le résultat du devis sous format texte brut (.txt)
    private static void sauvegarder(Batiment b, double t) {
        // Le paramètre "true" permet d'écrire à la suite du fichier existant sans l'écraser
        try (PrintWriter pw = new PrintWriter(new FileWriter("ResultatDevis.txt", true))) {
            pw.println("\n===========================================");
            pw.println("DEVIS DU BATIMENT : " + b.getId().toUpperCase());
            pw.println("Date de generation : " + new Date());
            pw.println("Surface totale au sol : " + b.surfaceSolBatiment() + " m2");
            pw.println("Montant Total : " + t + " Euros HT");
            pw.println("===========================================");

            if (b instanceof Maison) {
                for (Piece p : ((Maison)b).getPieces()) {
                    if (p != null) ecrirePiece(pw, p);
                }
            } else {
                for (Niveau n : ((Immeuble)b).getNiveaux()) {
                    if (n == null) continue;
                    pw.println("\n > NIVEAU " + n.getId() + " (Surf: " + n.surfaceSolNiveau() + "m2)");
                    for (Appartement app : n.getApparts()) {
                        if (app == null) continue;
                        pw.println("   - APPARTEMENT " + app.getId() + " (Surf: " + app.surfaceSolAppartement() + "m2)");
                        for (Piece p : app.getPieces()) {
                            if (p != null) ecrirePiece(pw, p);
                        }
                    }
                }
            }
        } catch (Exception e) { 
            System.out.println("Erreur lors de la sauvegarde du devis."); 
        }
    }

    // Écrit le sous-détail des composants d'une pièce dans le fichier d'export
    private static void ecrirePiece(PrintWriter pw, Piece p) {
        pw.println("     PIECE n°" + p.getId() + " (Sol Brut: " + String.format("%.2f", p.surfaceSol()) + " m2)");
        if (p.getSol().getRevetement() != null) {
            pw.println("       Rev. Sol : " + p.getSol().getRevetement().getDesignation());
        }
        if (p.getSol().getNbT() > 0) {
            pw.println("       Surface Nette Sol : " + p.getSol().surfaceNette(p.surfaceSol()) + " m2");
        }

        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i];
            if (m != null) {
                pw.print("       Mur " + (i+1) + " (Long: " + String.format("%.2f", m.longueur()) + "m)");
                if (m.getRevetement() != null) {
                    pw.print(" - Rev: " + m.getRevetement().getDesignation());
                }
                pw.println();
            }
        }
    }
}