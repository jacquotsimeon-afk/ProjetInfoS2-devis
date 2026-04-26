/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
import java.io.*;
import java.util.Date;

public class ProjetInfoS2Devisbatiment {
    private static Revetement[] catalogue = new Revetement[200];
    private static int nbRev = 0;

    public static void main(String[] args) {
        chargerCatalogue("CatalogueRevetements.txt");
        GestionDemande gd = new GestionDemande(catalogue, nbRev);
        Batiment bat = gd.creerBatiment();
        
        double total = bat.devisBatiment();
        System.out.println("\n--- RESULTAT CONSOLE ---");
        System.out.println("Devis estimatif total : " + total + " Euros");
        
        sauvegarder(bat, total);
    }

    private static void chargerCatalogue(String f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); String l;
            while ((l = br.readLine()) != null) {
                String[] t = l.split(";");
                int id = Integer.parseInt(t[0]);
                double p = Double.parseDouble(t[5]);
                catalogue[nbRev++] = new Peinture(id, t[1], p, t[2].equals("1"), t[3].equals("1"), t[4].equals("1"));
            }
        } catch (Exception e) { System.out.println("Erreur catalogue"); }
    }

   private static void sauvegarder(Batiment b, double t) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("ResultatDevis.txt", true))) {
            pw.println("\n===========================================");
            pw.println("DEVIS GENERE LE : " + new java.util.Date());
            pw.println("NOM DU BATIMENT : " + b.getId());
            pw.println("SURFACE AU SOL TOTALE DU BATIMENT : " + String.format("%.2f", b.surfaceSolBatiment()) + " m2");
            pw.println("DEVIS TOTAL     : " + t + " Euros");
            pw.println("===========================================");
            
            if (b instanceof Maison) {
                Maison m = (Maison) b;
                for (Piece p : m.getPieces()) {
                    if (p != null) ecrirePiece(pw, p);
                }
            } else if (b instanceof Immeuble) {
                Immeuble imm = (Immeuble) b;
                for (Niveau n : imm.getNiveaux()) {
                    if (n != null) {
                        pw.println("\n > NIVEAU " + n.getId() + " (H: " + n.getHauteur() + "m)");
                        pw.println("   SURFACE AU SOL DU NIVEAU : " + String.format("%.2f", n.surfaceSolNiveau()) + " m2");
                        
                        for (Appartement app : n.getApparts()) {
                            if (app != null) {
                                pw.println("   - APPARTEMENT " + app.getId() + " (Surface sol: " + String.format("%.2f", app.surfaceSolAppartement()) + " m2)");
                                for (Piece p : app.getPieces()) {
                                    if (p != null) ecrirePiece(pw, p);
                                }
                            }
                        }
                    }
                }
            }
            pw.println("\n--- FIN DU DEVIS ---\n");
            System.out.println("Le detail des surfaces et des revetements a ete exporte dans ResultatDevis.txt");
        } catch (Exception e) { 
            System.out.println("Erreur lors de la sauvegarde."); 
        }
    }

    private static void ecrirePiece(PrintWriter pw, Piece p) {
        pw.println("     PIECE n°" + p.getId());
        pw.println("       Surface au sol de la piece : " + String.format("%.2f", p.surfaceSol()) + " m2");
        
        // Revetement Sol et Plafond
        if (p.getSol().getRevetement() != null) {
            pw.println("       Revetement SOL     : " + p.getSol().getRevetement().getDesignation());
        }
        if (p.getPlafond().getRevetement() != null) {
            pw.println("       Revetement PLAFOND : " + p.getPlafond().getRevetement().getDesignation());
        }

        // Detail des murs
        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i];
            if (m != null) {
                pw.print("       Mur " + (i+1) + " (Long: " + String.format("%.2f", m.longueur()) + "m)");
                if (m.getRevetement() != null) pw.print(" - Revetement: " + m.getRevetement().getDesignation());
                pw.println("");
                
                // On liste les ouvertures
                int nbOuv = m.getNbO();
                if (nbOuv > 0) {
                    for (int k = 0; k < nbOuv; k++) {
                        pw.println("         * Ouverture " + (k+1) + " : " + m.getOuvertures()[k].getNom());
                    }
                }
            }
        }
    }
}