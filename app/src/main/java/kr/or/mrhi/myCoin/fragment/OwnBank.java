package kr.or.mrhi.myCoin.fragment;

import static kr.or.mrhi.myCoin.MainActivity.namePositionMap;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.List;

import kr.or.mrhi.myCoin.DBController;
import kr.or.mrhi.myCoin.R;
import kr.or.mrhi.myCoin.adapter.WalletAdapter;
import kr.or.mrhi.myCoin.model.Transaction;
import kr.or.mrhi.myCoin.viewModel.CoinViewModel;


public class OwnBank extends Fragment implements OnChartValueSelectedListener {

    private TextView textTotalBuyCount, textTotalEvaluationCount, textEvaluationProfitCount, textYieldCount, holdings, KRWHoldings;
    private double totalBuyCount, evaluationProfitCount, totalEvaluationCount, yieldCount;
    private PieChart pieChart;

    private DBController dbController;
    private List<Transaction> transactionList;
    private CoinViewModel model;
    private List<String> myCoinName;
    private List<Integer> myCoinPrice;
    private List<String> myCoinAmong;
    private List<String> priceList;
    private Transaction transaction;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_own_bank, container, false);
        dbController = new DBController(getActivity().getApplicationContext());
        model = new ViewModelProvider(requireActivity()).get(CoinViewModel.class);
        priceList = new ArrayList<>();
        textTotalBuyCount = view.findViewById(R.id.textTotalBuyCount);
        textTotalEvaluationCount = view.findViewById(R.id.textTotalEvaluationCount);
        textEvaluationProfitCount = view.findViewById(R.id.textEvaluationProfitCount);
        textYieldCount = view.findViewById(R.id.textYieldCount);
        holdings = view.findViewById(R.id.holdings);
        KRWHoldings = view.findViewById(R.id.KRWHoldings);
        listView = view.findViewById(R.id.mycoinlist);
        pieChart = view.findViewById(R.id.pieChart);


        WalletAdapter adapter = new WalletAdapter(dbController.getMyWallet());
        listView.setAdapter(adapter);


        transactionList = dbController.getMyWallet();
        priceList = new ArrayList<>();
        myCoinName = new ArrayList<>();
        myCoinAmong = new ArrayList<>();
        myCoinPrice = new ArrayList<>();
        for (int i = 0; i < transactionList.size(); i++) {
            myCoinName.add(transactionList.get(i).getCoinName());
            myCoinAmong.add(transactionList.get(i).getQuantity());
            myCoinPrice.add(transactionList.get(i).getBalance());
        }

        model.getTransactionCoinData("BTC").observe(requireActivity(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> transactionData) {
                double transactionPrice = 0.0;
                double buyCount = 0.0;
                double curruntPrice = 0.0;
                int balance = 0;
                totalBuyCount = 0.0;
                evaluationProfitCount = 0.0;

                for (Integer i : myCoinPrice) {
                    balance += i;
                }

                for (int i = 0; i < transactionList.size(); i++) {
                    if (namePositionMap.get(myCoinName.get(0)) != null) {
                        priceList.add(transactionData.get(namePositionMap.get(myCoinName.get(i))));
                    }
                }

                if (!priceList.isEmpty() && transactionList.size() != 0) {
                    for (int i = 0; i < transactionList.size(); i++) {
                        transactionPrice = Double.parseDouble(priceList.get(i));
                        buyCount = Double.parseDouble(myCoinAmong.get(i));

                        totalBuyCount += transactionPrice * buyCount;//총매수
                        curruntPrice = Double.parseDouble(transactionData.get(namePositionMap.get(myCoinName.get(i))));
                        evaluationProfitCount += curruntPrice * buyCount;//총평가
                        Log.i("총매수", transactionPrice + "/" + buyCount);
                    }
                    textTotalBuyCount.setText(String.format("%.2f", totalBuyCount));//총매수
                    textTotalEvaluationCount.setText(String.format("%.0f", evaluationProfitCount));//총평가
                    textEvaluationProfitCount.setText(String.format("%.0f", evaluationProfitCount - totalBuyCount));
                    textYieldCount.setText(String.format("%.2f%%", ((evaluationProfitCount - totalBuyCount) / totalBuyCount * 100)));
                }


                if (transactionList.size() != 0) {
                    KRWHoldings.setText(String.valueOf(balance));
                    holdings.setText(String.format("%.0f", evaluationProfitCount + Double.parseDouble(KRWHoldings.getText().toString())));
                }
            }
        });
        model.refrashTransactionDataThread(myCoinName.toArray(new String[myCoinName.size()]));

        DBController dbController = new DBController(requireContext());
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Transaction> entries1 = (ArrayList<Transaction>) dbController.getMyWallet();


        for (int i = 0; i < entries1.size(); i++) {
            Log.i("그래프", entries1.get(i).getCoinName());
            entries.add(new PieEntry(Float.parseFloat(entries1.get(i).getQuantity()),
                    entries1.get(i).getCoinName(), null));
        }


        PieDataSet dataSet = new PieDataSet(entries, "purchase");
        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setCenterText("구매내역");

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);

        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);

        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.setDrawCenterText(true);

        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        pieChart.setOnChartValueSelectedListener(this);

        pieChart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        return view;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

}