package com.jesusrodri.localizacionproyecto;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

/**
 * Creado by chukk on 17/01/2016.
 */
public class MiFragmentPagerAdapter extends FragmentPagerAdapter{

    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"Datos Generales", "Mapa General"};
    //public Toolbar toolbarRuta, toolbarContactos;
    public  Fragment_Localizacion m1stFragment;
    public  Fragment_Mapa m2ndFragment; //Fragment del mapa


    public MiFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = null;
        switch (position) {
            case 0:
                f = Fragment_Localizacion.newInstance();
                break;
            case 1:
                f = Fragment_Mapa.newInstance();
                break;
        }
        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                m1stFragment = (Fragment_Localizacion) createdFragment;
                break;
            case 1: //Fragment del mapa
                m2ndFragment = (Fragment_Mapa) createdFragment;
                break;
        }
        return createdFragment;
    }
}
