package com.mycompany.projetinfos2.devisbatiment;
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
        System.out.println("\nDevis Total : " + total + " Euros");
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
            pw.println("DEVIS DU : " + new Date());
            pw.println("PROJET : " + b.getId());
            pw.println("SURFACE SOL TOTALE : " + String.format("%.2f", b.surfaceSolBatiment()) + " m2");
            pw.println("MONTANT TOTAL : " + t + " €");
            pw.println("===========================================");
            
            if (b instanceof Maison) {
                for (Piece p : ((Maison)b).getPieces()) if (p != null) ecrirePiece(pw, p);
            } else {
                for (Niveau n : ((Immeuble)b).getNiveaux()) {
                    if (n == null) continue;
                    pw.println("\n > NIVEAU " + n.getId() + " (Surf: " + n.surfaceSolNiveau() + "m2)");
                    for (Appartement app : n.getApparts()) {
                        if (app == null) continue;
                        pw.println("   - APPARTEMENT " + app.getId() + " (Surf: " + app.surfaceSolAppartement() + "m2)");
                        for (Piece p : app.getPieces()) if (p != null) ecrirePiece(pw, p);
                    }
                }
            }
        } catch (Exception e) { System.out.println("Erreur sauvegarde"); }
    }

    private static void ecrirePiece(PrintWriter pw, Piece p) {
        pw.println("     PIECE n°" + p.getId() + " (Sol Brut: " + String.format("%.2f", p.surfaceSol()) + " m2)");
        if (p.getSol().getRevetement() != null) pw.println("       Rev. Sol : " + p.getSol().getRevetement().getDesignation());
        if (p.getSol().getNbT() > 0) pw.println("       Surface Nette Sol : " + p.getSol().surfaceNette(p.surfaceSol()) + " m2");

        for (int i = 0; i < p.getMurs().length; i++) {
            Mur m = p.getMurs()[i];
            if (m != null) {
                pw.println("       Mur " + (i+1) + " : [" + m.getDebut() + "->" + m.getFin() + "] Long: " + String.format("%.2f", m.longueur()) + "m");
            }
        }
    }
}