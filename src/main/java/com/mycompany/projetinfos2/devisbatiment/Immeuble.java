package com.mycompany.projetinfos2.devisbatiment;

public class Immeuble extends Batiment {
    private Niveau[] niveaux;
    private int nbNiveauxRealises = 0;

    public Immeuble(String id, int nbrN) {
        this.idBatiment = id; this.niveaux = new Niveau[nbrN];
    }

    public void ajouterNiveau(Niveau n) { if (nbNiveauxRealises < niveaux.length) niveaux[nbNiveauxRealises++] = n; }

    @Override public double devisBatiment() {
        double total = 0;
        for (int i = 0; i < nbNiveauxRealises; i++) total += niveaux[i].devisNiveau();
        return total;
    }

    @Override public double surfaceSolBatiment() {
        double total = 0;
        for (int i = 0; i < nbNiveauxRealises; i++) total += niveaux[i].surfaceSolNiveau();
        return total;
    }
    public Niveau[] getNiveaux() { return niveaux; }
}