package com.bleblind.support;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bleblind.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Fred on 2017/1/11.
 */
public class ScanListAdapter extends ArrayAdapter<BluetoothDevice> {

    private Context ctx;
    private int layoutResID;

    public ScanListAdapter(Context context, int layoutResourceId, List<BluetoothDevice> deviceList){
        super(context, layoutResourceId, deviceList);
        this.ctx = context;
        this.layoutResID = layoutResourceId;
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
            convertView = inflater.inflate(layoutResID, parent, false);
        }
        TextView tvName = (TextView)convertView.findViewById(R.id.tv_device_name);
        TextView tvAddress = (TextView)convertView.findViewById(R.id.tv_device_address);
        BluetoothDevice device = getItem(position);
        if(device.getName()!=null) {
            tvName.setText(getItem(position).getName());
        }
        if(device.getAddress()!=null) {
            tvAddress.setText(getItem(position).getAddress());
        }
        return convertView;
    }

}
