/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Peinture extends Revetement {
    public Peinture(int id, String nom, double p, boolean m, boolean s, boolean pl) {
        this.idRevetement = id; this.designation = nom; this.prix = p;
        this.pourMur = m; this.pourSol = s; this.pourPlafond = pl;
    }
    @Override public double montant(double surface) { return surface * prix; }
}