(ns license-form.core
  (:refer-clojure)
  (:require [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.element :refer :all]
            [clojure.data.json :as json]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn now [] (java.util.Date.))


(defn build-part-none
  "Creates the simple \"no license\" part"
  []
  [:div {:id "oer-license-selector-part-none"}
   [:p [:b "Keine Lizenz"]]
   [:p "Nutzung und Quellenangabe gemäß den im Medium genannten Bedinungen bzw. gemäß der allgemeingültigen gesetzlichen Regelung (UrhG)."]])


(defn build-part-cc
  "Creates the Creative Commons licens picker part of the form"
  []
  [:div {:id "oer-license-selector-part-cc",
         :style "display:none;"}
   [:p [:b "Creative Commons"]]
   [:p "Hier muss noch ein toller CC picker hin."]])


(defn build-part-free
  "Creates the Public Domain license part of the form."
  []
  [:div {:id "oer-license-selector-part-free",
         :style "display:none;"}
   [:p [:b "CC0 / Public Domain"]]
   [:p "Hier muss noch ein CC0 / PDM picker hin."]])


(defn build-part-custom
  "Creates the part of the license selector which offers a textarea
  to input a custom license text."
  []
  [:div {:id "oer-license-selector-part-custom",
         :style "display:none;"}
   [:p [:b "Benutzerdefinierte Lizenz"]]
   [:textarea {:id "oer-license-selector-custom-input"
               :placeholder "Lizenzbedingungen definieren ..."
               :onchange "oerLicenseSelector.chooseCustomLicense()"}]])


(defn build-base-select
  "Constructs the select element which will let the user choose
  style of license selection"
  []
  [:select {:id "oer-license-selector-base"
            :onchange "oerLicenseSelector.onBaseChanged()"}
   [:option {:value :none} "Keine Lizenz gewählt"]
   [:option {:value :cc} "Creative Commons"]
   [:option {:value :free} "Gemeinfrei (CC0) / Public Domain"]
   [:option {:value :custom} "Eigene Lizenz definieren"]])

(def selector-js
  (str
   "var oerLicenseSelector={"
     "setLicense:function(v){"
       "document.getElementById('oer-license-selector-license').value=v"
     "},"

     "setCustom:function(v){"
       "document.getElementById('oer-license-selector-custom').value=v"
     "},"

     "chooseNoLicense:function(){"
       "oerLicenseSelector.setLicense('');"
       "oerLicenseSelector.setCustom('')"
     "},"

     "chooseCCLicense:function(){"
       "oerLicenseSelector.setLicense('');"
       "oerLicenseSelector.setCustom('')"
     "},"

     "chooseFreeLicense:function(){"
       "oerLicenseSelector.setLicense('CC0 / Public Domain');"
       "oerLicenseSelector.setCustom('')"
     "},"

     "chooseCustomLicense:function(){"
       "oerLicenseSelector.setLicense('Eigene Lizenz');"
       "oerLicenseSelector.setCustom("
         "document.getElementById('oer-license-selector-custom-input').value)"
     "},"

     "onBaseChanged:function(){"
       "var e=document.getElementById('oer-license-selector-base');"
       "var s=e.options[e.selectedIndex].value;"
       "var all=document.querySelectorAll('[id^=\"oer-license-selector-part-\"]');"
       "for(i=0;i<all.length;i++){all[i].style.display='none'}"
       "document.getElementById('oer-license-selector-part-'+s).style.display='';"
       "var m={"
         "none:oerLicenseSelector.chooseNoLicense,"
         "cc:oerLicenseSelector.chooseCCLicense,"
         "free:oerLicenseSelector.chooseFreeLicense,"
         "custom:oerLicenseSelector.chooseCustomLicense"
       "};"
       "m[s]()"
     "}"
   "}"))


(defn build-body
  "Constructs the html snippet body using the given parameters"
  [params]
  (println (now) ": " params)
  (html [:html
         [:head]
         [:body
          (javascript-tag selector-js)
          [:form {:action (get params :post-url "/"),
                  :method :POST}

           (build-base-select)
           [:br]
           (build-part-none)
           (build-part-cc)
           (build-part-free)
           (build-part-custom)
           [:input {:type :hidden,
                    :name :license,
                    :id "oer-license-selector-license"}]
           [:input {:type :hidden,
                    :name :custom
                    :id "oer-license-selector-custom"}]

           (submit-button "Submit")]]]))


(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (build-body (:params request))})

(defn wrap-timed [f]
  (fn [request]
    (time (f request))))

(def app (-> handler
             wrap-keyword-params
             wrap-params
             wrap-json-params
             wrap-timed))

(defonce server (run-jetty (var app) {:port 3000 :join? false}))

nil
