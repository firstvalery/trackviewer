package ru.samrtsarov.yandexgeo;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GetGeo {
	final static private String YANDEX_URL = "https://geocode-maps.yandex.ru/1.x/?format=json&geocode=";
	
	public static String getGeoLocation(String address) {
		try{
			String str = Jsoup.connect(YANDEX_URL+address).ignoreContentType(true).get().body().text();
			Gson gson = new GsonBuilder().create();
			YandexGeoTranslate yandexGeoObj = gson.fromJson(str, YandexGeoTranslate.class);
			return gson.toJson(yandexGeoObj.getResponse().getGeoObjectCollection().getFeatureMember().get(0).getGeoObject().getPoint());
		}catch(IOException e) {
			return e.getMessage();
		}
	}
	
	public static String getAdress(String location) {//location format 12.3456789,12.3456789
		try{
			String str = Jsoup.connect(YANDEX_URL + location).ignoreContentType(true).get().body().text();
			YandexGeoTranslate yandexGeoObj = new GsonBuilder().create().fromJson(str, YandexGeoTranslate.class);

			Map<String, String> adr = yandexGeoObj.getResponse().getGeoObjectCollection()
					.getFeatureMember().get(0).getGeoObject()
					  .getMetaDataProperty().getGeocoderMetaData()
					    .getAddress().getComponents().stream()
					    .collect(Collectors.toMap(Component::getKind, Component::getName, (oldValue, newValue)->oldValue));
			 return (adr.get("locality")+" "+adr.get("street")+ " " + adr.get("house")).replaceAll("null", "");
		}catch(Exception e) {
			return "адрес не найден";
		}
	}
	

	public static YandexGeoTranslate getYandexGeoTranslateObj(String address) {
		try{
			String str;
			str = Jsoup.connect(YANDEX_URL+address).ignoreContentType(true).get().body().text();
			Gson gson = new GsonBuilder().create();
			
			return gson.fromJson(str, YandexGeoTranslate.class);
			}catch(Exception e) {
				return null;
			}
		}
}
