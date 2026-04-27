
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Tremie extends Ouverture {
    private int idTremie;
    private double surface;
    public Tremie(int id, double s) { this.idTremie = id; this.surface = s; }
    @Override public double surface() { return surface; }
    @Override public String getNom() { return "Tremie"; }
}