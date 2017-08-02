(ns license-form.core
  (:gen-class)
  (:refer-clojure)
  (:require [clojure.string :as string]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.element :refer :all]
            [clojure.data.json :as json]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def selector-js
  (slurp (clojure.java.io/resource "script.js")))

(defn- now [] (java.util.Date.))


;; HELPER FUNCTIONS AND MACROS


(defmacro is-true?
  "Returns true if the given value is Boolean.TRUE,
   or a String that matches 'true' case-insensitive."
  [v]
  `(Boolean/valueOf ~v))

(defmacro id-prefix
  "Returns the id-prefix for html elements, based on the
   'id-prefix' parameter"
  [params]
  `(:id-prefix ~params "license-selector-"))

(defmacro gen-id
  "Generates an html element id using the 'id-prefix' parameter
   and the provided postfix"
  [params postfix]
  `(str (id-prefix ~params) ~postfix))

(defmacro license-name-field-id
  "Generates the html element id for the hidden input field that
   contains the license name"
  [params]
  `(:license-name-field-id ~params (gen-id ~params "license-name")))

(defmacro license-text-field-id
  "Generates the html element id for the hidden input field that
   contains the custom license text"
  [params]
  `(:license-text-field-id ~params (gen-id ~params "license-text")))

(defmacro js-object-var
  "Generates the name of the javascript object which holds
   all necessary functions."
  [params]
  `(:js-object-var ~params "selector"))

(defmacro js-call
  "Generates a JS string in the form of 'js-object-var{functioncall}'."
  [params function-call]
  `(str (js-object-var ~params) \. ~function-call))

(defmacro prepare-javascript
  "Returns the javascript code that the form will use.
   Replaces all placeholders in the code based on the supplied parameters."
  [params]
  `(-> selector-js
       (string/replace #"##object_var##" (js-object-var ~params))
       (string/replace #"##id_prefix##" (id-prefix ~params))
       (string/replace #"##id_license_name##" (license-name-field-id ~params))
       (string/replace #"##id_license_text##" (license-text-field-id ~params))))


;; FORM BUILDING FUNCTIONS


(defn- build-part-none
  "Creates the simple \"no license\" part of the form"
  [params]
  [:div {:id (gen-id params "part-none")}
   [:p [:b "Keine Lizenz"]]
   [:p "Nutzung und Quellenangabe gemäß den im Medium genannten Bedinungen bzw. gemäß der allgemeingültigen gesetzlichen Regelung (UrhG)."]])


(defn- build-part-cc
  "Creates the Creative Commons licens picker part of the form"
  [params]
  [:div {:id (gen-id params "part-cc"),
         :style "display:none;"}
   [:p [:b "Creative Commons"]]
   [:p "Hier muss noch ein toller CC picker hin."]])


(defn- build-part-free
  "Creates the Public Domain license part of the form."
  [params]
  [:div {:id (gen-id params "part-free"),
         :style "display:none;"}
   [:p [:b "CC0 / Public Domain"]]
   [:p "Hier muss noch ein CC0 / PDM picker hin."]])


(defn- build-part-custom
  "Creates the part of the license selector which offers a textarea
  to input a custom license text."
  [params]
  [:div {:id (gen-id params "part-custom"),
         :style "display:none;"}
   [:p [:b "Benutzerdefinierte Lizenz"]]
   [:textarea {:id (gen-id params "custom-input")
               :placeholder "Lizenzbedingungen definieren ..."
               :onchange (js-call params "chooseCustomLicense()")}]])


(defn- build-base-select
  "Constructs the select element which will let the user choose
  style of license selection"
  [params]
  [:select {:id (gen-id params "base")
            :onchange (js-call params "onBaseChanged()")}
   [:option {:value :none} "Keine Lizenz gewählt"]
   [:option {:value :cc} "Creative Commons"]
   [:option {:value :free} "Gemeinfrei (CC0) / Public Domain"]
   (when (is-true? (:allow-custom-license params true))
     [:option {:value :custom} "Eigene Lizenz definieren"])])


(defn- build-body
  "Constructs the html snippet body using the given parameters"
  [params]
  (println (now) ": " params)
  (html [:html
         [:head [:meta {:charset :UTF-8}]]
         [:body
          (javascript-tag (prepare-javascript params))
          [:form {:action (:post-url params "/"),
                  :method :POST}

           (build-base-select params)
           [:br]
           (build-part-none params)
           (build-part-cc params)
           (build-part-free params)

           (when (is-true? (:allow-custom-license params true))
             (build-part-custom params))

           [:input {:type :hidden,
                    :name (:license-name-field-name params "license-name"),
                    :id (license-name-field-id params)}]
           [:input {:type :hidden,
                    :name (:license-text-field-name params "license-text"),
                    :id (license-text-field-id params)}]

           (when (is-true? (:with-submit params true))
             (submit-button "Submit"))]]]))


;; JETTY SERVER FUNCTIONS


(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (build-body (:params request))})


(defn- wrap-timed [f]
  (fn [request]
    (time (f request))))

(def app (-> handler
             wrap-keyword-params
             wrap-params
             wrap-json-params
             wrap-timed))


(defn -main [& args]
  (defonce server (run-jetty (var app) {:port 3000 :join? false})))

