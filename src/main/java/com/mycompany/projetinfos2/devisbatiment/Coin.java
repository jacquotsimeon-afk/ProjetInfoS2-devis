package com.mycompany.projetinfos2.devisbatiment;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Siméon
 */
public class Coin {
    private int idCoin;
    private double cx;
    private double cy;

    public Coin(int id, double x, double y) {
        this.idCoin = id;
        this.cx = x;
        this.cy = y;
    }

    public double getCx() { return cx; }
    public double getCy() { return cy; }
    public int getId() { return idCoin; }

    @Override
    public String toString() {
        return "Point " + idCoin + " (" + cx + "m, " + cy + "m)";
    }
}