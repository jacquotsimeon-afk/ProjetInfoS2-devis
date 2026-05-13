package com.mycompany.projetinfos2.devisbatiment;

public class Mur {
    private int idMur;
    private Coin debut, fin;
    private Ouverture[] ouvertures;
    private int nbO = 0;
    private Revetement revetement;

    public Mur(int id, Coin d, Coin f, int maxO) {
        this.idMur = id; this.debut = d; this.fin = f;
        this.ouvertures = new Ouverture[maxO];
    }

    public void ajouterOuverture(Ouverture o) { if (nbO < ouvertures.length) ouvertures[nbO++] = o; }
    public void appliquerRevetement(Revetement r) { this.revetement = r; }

    public double longueur() {
        return Math.sqrt(Math.pow(fin.getCx()-debut.getCx(), 2) + Math.pow(fin.getCy()-debut.getCy(), 2));
    }

    public double surface(double h) {
        double s = longueur() * h;
        for (int i = 0; i < nbO; i++) s -= ouvertures[i].surface();
        return Math.max(0, s);
    }

    public double devisMur(double h) { return (revetement != null) ? revetement.montant(surface(h)) : 0; }

    public Revetement getRevetement() { return revetement; }
    public Coin getDebut() { return debut; }
    public Coin getFin() { return fin; }
    public Ouverture[] getOuvertures() { return ouvertures; }
    public int getNbO() { return nbO; }
    public int getId() { 
        return idMur; 
    }
}