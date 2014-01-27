(ns release-date.goodreads
  (:use [clojure.data.zip.xml :only (attr tag= text xml->)])
  [:require [clojure.xml :as xml]
            [clojure.zip :as zip]])

(read-string (slurp "./goodreads-key.txt"))

(defn make-url
  "Creates goodreads api call url with key on it.  Arguments for the call still need to be applied"
  [call]
  (format "https://www.goodreads.com/%s?key=%s", call, apikey))

(defn make-search-url
  "Creates a valid search api url for the given query"
  [query]
  (str (make-url "search.xml") "&q=" query))

(defn search
  [query]
  (xml/parse (make-search-url query)))

;TESTING

;stash some results
(def results (search "mistborn"))

(def zipped (zip/xml-zip results))

;gets a list of the ids of potentially matching books
(def ids (xml-> zipped :search :results :work (tag= :id) text))


;just printing out results
;(clojure.pprint/pprint results)
