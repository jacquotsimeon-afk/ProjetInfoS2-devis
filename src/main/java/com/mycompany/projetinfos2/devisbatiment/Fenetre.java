/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Fenetre extends Ouverture {
    private int idFenetre;

    public Fenetre(int id) { this.idFenetre = id; }

    @Override
    public double surface() {
        return 1.20 * 1.20; // Dimensions standard m
    }

    @Override
    public String getNom() { return "Fenetre"; }
}