package com.example.authvk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private String[] scope = new String[]{VKScope.FRIENDS};
    private ListView listView;
    private TextView userNameTxt;
    private TextView userCityTxt;
    private TextView userSexTxt;
    private TextView friendText;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userImage = findViewById(R.id.userImage);
        userNameTxt = findViewById(R.id.userNameTxt);
        userCityTxt = findViewById(R.id.userCityTxt);
        userSexTxt = findViewById(R.id.userSexTxt);
        friendText = findViewById(R.id.friendText);
        listView = findViewById(R.id.list_view);

        noAccessLogin();

        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(this, scope);
        } else {
            userItem();
            frienItem();
        }
    }

    private void noAccessLogin() {
        Picasso.get().load("https://vk.com/images/camera_400.png").into(userImage);
        userNameTxt.setText(R.string.no_access_login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.login:
                if (!VKSdk.isLoggedIn()) {
                    VKSdk.login(this, scope);
                    friendText.setVisibility(View.VISIBLE);
                    noAccessLogin();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.is_login), Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                //сохраняем Токен
                res.saveTokenToSharedPreferences(MainActivity.this, Constants.KEY_TOKEN);

                userItem();
                frienItem();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this, " Что-то пошло не так!", Toast.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //создаем свою страницу(фото, имя, город, пол)
    private void userItem() {
        //получаем фото, имя, пол и город пользователя
        VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name,last_name,photo_400_orig,city,sex")).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {

                VKApiUser user = ((VKList<VKApiUser>) response.parsedModel).get(0);
                try {
                    String name, city;
                    JSONObject cityJson = user.fields.getJSONObject("city");
                    JSONObject userJson = user.fields;

                    name = userJson.getString("first_name") + " " + userJson.getString("last_name");
                    userNameTxt.setText(name);

                    Picasso.get().load(userJson.getString("photo_400_orig")).into(userImage);

                    city = getResources().getText(R.string.city) + cityJson.getString("title");
                    userCityTxt.setText(city);

                    userSexTxt.setText(userJson.getString("sex").
                            equals("2") ? getResources().getText(R.string.sex_male) : getResources().getText(R.string.sex_female));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //создаем 5 случайных друзей(фото и имя)
    private void frienItem() {
        //получаем фото и имя друзей
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "photo_200"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                //инициализируем список друзей
                List<FriendVK> friendVKList = new ArrayList<>();
                FriendVK friendVK;
                VKList list = (VKList) response.parsedModel;
                VKApiUser friend;

                        /*если больше 5 друзей, то выводим на экран 5 случайных друзей,
                        иначе выводим всех друзей*/
                if (list.size() > 5) {
                    //получаем список случайных неповторяющихся чисел
                    Set<Integer> generated = getNumbers(list.size());

                    for (Integer integer : generated) {
                        friend = ((VKList<VKApiUser>) response.parsedModel).get(integer);
                        String name = friend.first_name + " " + friend.last_name;
                        friendVK = new FriendVK(name, friend.photo_200);
                        friendVKList.add(friendVK);
                    }
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        friend = ((VKList<VKApiUser>) response.parsedModel).get(i);
                        String name = friend.first_name + " " + friend.last_name;
                        friendVK = new FriendVK(name, friend.photo_200);
                        friendVKList.add(friendVK);
                    }
                }

                LayoutInflater inflater = getLayoutInflater();
                FriendVKAdapter adapter = new FriendVKAdapter(friendVKList
                        , inflater);
                listView.setAdapter(adapter);
            }
        });
    }

    //выходим из аккаунта
    public void logout() {
        if (VKSdk.isLoggedIn()) {
            VKSdk.logout();
            Toast.makeText(this, getResources().getString(R.string.exit_access), Toast.LENGTH_LONG).show();
            Picasso.get().load("https://vk.com/images/camera_400.png").into(userImage);
            userNameTxt.setText(getResources().getString(R.string.login_main));
            userCityTxt.setText("");
            userSexTxt.setText("");
            listView.setAdapter(null);
            friendText.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(this, getResources().getString(R.string.no_login), Toast.LENGTH_LONG).show();
        }
    }

    private Set<Integer> getNumbers(int size) {
        Set<Integer> generated = new HashSet<>();
        Random r = new Random();
        while (generated.size() < 5) {
            generated.add(r.nextInt(size));
        }
        return generated;
    }

}
