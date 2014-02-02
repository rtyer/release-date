(ns release-date.goodreads
  (:use [clojure.data.zip.xml :only (attr tag= text text= xml->)])
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.java.io :as io])
  (:import [java.io.PushbackReader]))

;(read-string (slurp "./goodreads-key.txt"))
(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(def config (load-config "./goodreads-key.txt"))

(defn make-url
  "Creates goodreads api call url with key on it.  Arguments for the call still need to be applied"
  [call]
  (format "https://www.goodreads.com/%s?key=%s", call, (:apikey config)))

; search
(defn make-search-url
  "Creates a valid search api url for the given query"
  [query]
  (str (make-url "search.xml") "&q=" query))

(defn create-sample-file
  "Copies contents from url into file.  Useful for shitty api's like goodreads, that don't provide sample results or good api docs"
  [url file]
  (with-open [in (io/input-stream url)
              out (io/output-stream file)]
    (io/copy in out)))

(defn create-sample-search
  "pulls the resulting xml for the search query into <query>.xml"
  [query]
  (create-sample-file (make-search-url query) (io/file (str query ".xml"))))

(defn works->map
  "converts the works section of xml to a map"
  [works]
  (let [id (Integer/valueOf (first (xml-> works :id text)))
        title (first (xml-> works :best_book :title text))
        author (first (xml-> works :best_book :author :name text))
        img-url (first (xml-> works :best_book :image_url text))]
    {:id id
     :title title
     :author author
     :img img-url}))

(defn search
  [query]
  (let [result-xml (xml/parse (make-search-url query))
        zipper (zip/xml-zip result-xml)
        works (map works->map (xml-> zipper :search :results :work))]
    (clojure.pprint/pprint works)))

;series
;66322 id

(defn make-series-by-work-url
  "Creates a valid call to the search series by work id.  Note:  This is NOT the BOOK id, but the WORK id."
  [work-id]
  (str (make-url "series/work.xml") "&id=" work-id))

;TESTING

(create-sample-file (make-series-by-work-url 66322) (io/file "mistborn-series.xml"))
(create-sample-file (make-series-by-work-url 2008238) (io/file "wot-series.xml"))
;stash some results
(create-sample-search "mistborn")
(create-sample-search "The%20Eye%20of%20the%20World");needs url encoding.  should change...

(search "mistborn")

(map (fn [elt] (or (:tag elt) elt)) (xml-seq results))

;(def zipped (zip/xml-zip results))
;
;(map works->map (xml-> zipped :search :results :work))
;
;(defn works->map
  ;"converts the works section of xml to a map"
  ;[works]
  ;(let [id (Integer/valueOf (first (xml-> works :id text)))
        ;title (first (xml-> works :best_book :title text))
        ;author (first (xml-> works :best_book :author :name text))
        ;img-url (first (xml-> works :best_book :image_url text))]
    ;{:id id
     ;:title title
     ;:author author
     ;:img img-url}))

;gets a list of the ids of potentially matching books
(def ids (xml-> zipped :search :results :work (tag= :id) text))
