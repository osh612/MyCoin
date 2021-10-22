package kr.or.mrhi.myCoin.viewModel;

import static kr.or.mrhi.myCoin.MainActivity.namePositionMap;
import static kr.or.mrhi.myCoin.MainActivity.stringSymbol;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import kr.or.mrhi.myCoin.POJO.OrderBookData;
import kr.or.mrhi.myCoin.POJO.TransactionData;
import kr.or.mrhi.myCoin.POJO.TickerPOJOData;
import kr.or.mrhi.myCoin.retrofit.CoinRetrofit;
import kr.or.mrhi.myCoin.POJO.TickerData;
import kr.or.mrhi.myCoin.POJO.CandleCoinData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoinViewModel extends ViewModel {
    private MutableLiveData<List<CandleCoinData>> candleCoinData;
    private MutableLiveData<TickerData> tickerCoinData;
    private MutableLiveData<TickerPOJOData> tickerDTOData;
    private MutableLiveData<List<OrderBookData>> orderbookCoinData;
    private MutableLiveData<List<String>> transactionCoinData;
    private MutableLiveData<String> searchName;

    private NewCandleData newCandleData;
    private NewTickerData newTickerData;
    private NewOrderBookData orderBookData;
    private NewTransactionData newTransactionData;
    private TickerDTO tickerDTO;
    private static final int LATELYDATA = 19;
    private List<String> priceList;
    private boolean stopFlag;

    public CoinViewModel() {
        this.searchName = new MutableLiveData<String>();
        this.newCandleData = new NewCandleData();
        this.newTickerData = new NewTickerData();
        this.orderBookData = new NewOrderBookData();
        this.newTransactionData = new NewTransactionData();
        this.tickerDTO = new TickerDTO();
        this.priceList= new ArrayList<>(20);
        for (int i=0; i<stringSymbol.length; i++){
            priceList.add("0.00");
        }
        stopFlag=false;
    }


    public MutableLiveData<String> getSearchName() {
        searchName.setValue("");
        Log.i("검색:getSearchName", searchName.getValue());
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName.setValue(searchName);
        Log.i("검색:setSearchName", this.searchName.getValue());
    }

    public MutableLiveData<List<CandleCoinData>> getCandleCoinData(String coinName, String intervals) {
        if (candleCoinData == null) {
            candleCoinData = new MutableLiveData<List<CandleCoinData>>();
        }
        newCandleData.refreshCoinData(coinName, intervals);
        return candleCoinData;
    }

    public MutableLiveData<TickerData> getTickerCoinData() {
        if (tickerCoinData == null) {
            tickerCoinData = new MutableLiveData<TickerData>();
        }
        newTickerData.refreshCoinData();

        return tickerCoinData;
    }

    public MutableLiveData<TickerPOJOData> getTickerDTO(String coinName) {
        if (tickerDTOData == null) {
            tickerDTOData = new MutableLiveData<TickerPOJOData>();
        }
        tickerDTO.refreshTickerDTO(coinName);

        return tickerDTOData;
    }

    public MutableLiveData<List<OrderBookData>> getOrderBookCoinData() {
        if (orderbookCoinData == null) {
            orderbookCoinData = new MutableLiveData<List<OrderBookData>>();
        }
        orderBookData.refreshOrderBookCoinData();

        return orderbookCoinData;
    }

    public MutableLiveData<List<String>> getTransactionCoinData(String coinName) {
        if (transactionCoinData == null) {
            transactionCoinData = new MutableLiveData<List<String>>();
        }
        newTransactionData.refreshTransactionCoinData(coinName);

        return transactionCoinData;
    }

    public void refrashTransactionDataThread(String[] coinNames) {
        stopFlag=true;
        Thread thread = new Thread(() -> {
            while (stopFlag) {
                for (int i = 0; i < coinNames.length; i++) {
                    synchronized (this) {
                        newTransactionData.refreshTransactionCoinData(coinNames[i]);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread() {
        this.stopFlag = false;
    }

    public class NewCandleData {

        @SerializedName("status")
        @Expose
        private String status;

        @SerializedName("data")
        @Expose
        private List<List<String>> data = null;

        public void refreshCoinData(@NonNull String coinName, String intervals) {

            CoinRetrofit.create()
                    .getCoinData(coinName.toUpperCase(), "KRW", intervals)
                    .enqueue(new Callback<NewCandleData>() {
                        @Override
                        public void onResponse(Call<NewCandleData> call, Response<NewCandleData> response) {
                            candleCoinData.setValue(makeCoinList(response));
                        }

                        @Override
                        public void onFailure(Call<NewCandleData> call, Throwable t) {
                            Log.i("이전코인", "실패 : " + t.fillInStackTrace());
                        }
                    });
        }

        @NonNull
        private List<CandleCoinData> makeCoinList(@NonNull Response<NewCandleData> response) {
            List<CandleCoinData> formerCoinList = new ArrayList<>();
            for (List<String> list : response.body().getData()) {
                CandleCoinData coin = new CandleCoinData(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5));
                formerCoinList.add(coin);
                Log.i("이전코인", coin.toString());
            }
            Log.i("이전코인", formerCoinList.size() + "");
            return formerCoinList;
        }

        private List<List<String>> getData() {
            return data;
        }
    }

    public class NewTickerData {
        @SerializedName("status")
        @Expose
        private String status;

        @SerializedName("data")
        @Expose
        private TickerData tickerData;

        private void refreshCoinData() {
            CoinRetrofit.create()
                    .getTickerCoinData("ALL", "KRW")
                    .enqueue(new Callback<NewTickerData>() {
                        @Override
                        public void onResponse(Call<NewTickerData> call, Response<NewTickerData> response) {
                            tickerCoinData.setValue(response.body().getNewData());
                            Log.i("현재코인", tickerCoinData.getValue().getBtc().getName());
                        }

                        @Override
                        public void onFailure(Call<NewTickerData> call, Throwable t) {
                            Log.i("현재코인", "실패 : " + t.fillInStackTrace());
                        }
                    });
        }




        private TickerData getNewData() {
            return tickerData;
        }
    }

    public class NewOrderBookData {

        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("data")
        @Expose
        private OrderBookData orderBookData;


        private void refreshOrderBookCoinData() {
            CoinRetrofit.create()
                    .getOrderBookCoinData("ALL", "KRW")
                    .enqueue(new Callback<NewOrderBookData>() {
                        @Override
                        public void onResponse(Call<NewOrderBookData> call, Response<NewOrderBookData> response) {
                            orderbookCoinData.setValue(makeNewcoinList(response));
                            Log.i("현재코인", orderbookCoinData.getValue().get(0).getBtc().getAsks().get(0).getPrice().toString());
                        }

                        @Override
                        public void onFailure(Call<NewOrderBookData> call, Throwable t) {
                            Log.i("현재코인", "실패 : " + t.fillInStackTrace());
                        }
                    });
        }

        @NonNull
        private List<OrderBookData> makeNewcoinList(@NonNull Response<NewOrderBookData> response) {
            List<OrderBookData> list = new ArrayList<>();
            list.add(response.body().getNewData());
            return list;
        }

        private OrderBookData getNewData() {
            return orderBookData;
        }

    }

    public class NewTransactionData {

        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("data")
        @Expose
        private List<TransactionData> data = null;


        private void refreshTransactionCoinData(String coinName) {

            CoinRetrofit.create()
                    .getTransactionCoinData(coinName)
                    .enqueue(new Callback<NewTransactionData>() {
                        @Override
                        public void onResponse(Call<NewTransactionData> call, Response<NewTransactionData> response) {
                            transactionCoinData.setValue(makeMapData(coinName, response));
                        }

                        @Override
                        public void onFailure(Call<NewTransactionData> call, Throwable t) {
                            Log.i("현재코인", "실패 : " + t.fillInStackTrace());
                        }
                    });
        }

        private List<String> makeMapData(String coinName, Response<NewTransactionData> response) {

            if (response.body() != null) {
                priceList.set(namePositionMap.get(coinName), response.body().getNewData().get(LATELYDATA).getPrice());

            }
            return priceList;
        }

        private List<TransactionData> getNewData() {
            return data;
        }
    }

    public class TickerDTO {

        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("data")
        @Expose
        private TickerPOJOData data;

        private void refreshTickerDTO(String coinName) {
            CoinRetrofit.create()
                    .getTickerDTO(coinName)
                    .enqueue(new Callback<TickerDTO>() {
                        @Override
                        public void onResponse(Call<TickerDTO> call, Response<TickerDTO> response) {
                            tickerDTOData.setValue(response.body().getData());
                            Log.i("현재코인", tickerDTOData.getValue().toString());
                        }
                        @Override
                        public void onFailure(Call<TickerDTO> call, Throwable t) {
                            Log.i("현재코인", "실패 : " + t.fillInStackTrace());
                        }
                    });
        }

        public TickerPOJOData getData() {
            return data;
        }

    }

}