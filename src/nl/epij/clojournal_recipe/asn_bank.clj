(ns nl.epij.clojournal-recipe.asn-bank
  "Clojournal recipe to turn CSV statements of the Dutch bank 'ASN Bank' into plain text accounting journals."
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [com.clojournal.alpha :as clojournal]
            [com.clojournal.alpha.virtual :as virtual]
            [com.clojournal.alpha.api :as clojournal.api])
  (:import (java.time LocalDate)
           (java.time.format DateTimeFormatter)
           (java.text NumberFormat)
           (java.util Locale)))

(def columns
  "The CSV file from ASN Bank comes without a header row, so we have to hardcode them."
  [:date
   nil
   :payee
   nil
   nil
   nil
   nil
   :balance-currency
   :balance-value
   :amount-currency
   :amount-value
   nil
   nil
   nil
   nil
   :transaction-id
   nil
   :memo
   nil])

(defn- merge-headers
  [headers row]
  (map-indexed #(vector %2 (nth row %1)) headers))

(defn- ->date
  [x]
  (LocalDate/parse x (DateTimeFormatter/ofPattern "dd-MM-yyyy")))

(defn- ->amount
  "Also replaces non-breaking space \u00A0 with a space."
  [currency amount]
  (let [instances {"EUR" (Locale. "nl" "NL")
                   "USD" (Locale. "en" "US")}]
    (-> (doto
          (NumberFormat/getCurrencyInstance (get instances currency)))
        (.format amount)
        (str/replace "\u00A0" " "))))

(defn- check-id-uniqueness!
  [xs]
  (let [total      (count xs)
        unique-ids (count (set (map ::clojournal/transaction-id xs)))]
    (assert (= total unique-ids)
            (format "Total: %s; unique-ids: %s" total unique-ids))
    xs))

(defn- print!
  [f x]
  (spit f (clojournal.api/journal x))
  x)

(defn row->tx
  [columns account row]
  (let [{:keys [date
                payee
                amount-currency
                amount-value
                transaction-id
                memo]}
        (->> row (merge-headers columns) (into {}))]
    {::clojournal/date           (str (->date date))
     ::clojournal/transaction-id transaction-id
     ::clojournal/payee          payee
     ::clojournal/memo           memo
     ::clojournal/postings       [{::clojournal/account account
                                   ::clojournal/amount  (->amount amount-currency (BigDecimal. ^String amount-value))
                                   ::clojournal/virtual ::virtual/unbalanced}]}))

(defn rows->txs
  [columns account rows]
  (->> rows
       (map (partial row->tx columns account))
       check-id-uniqueness!))

(defn -main
  [account in out]
  (->> (csv/read-csv (io/reader in))
       (rows->txs columns account)
       (print! out)))

(comment

  (slurp (io/resource "2020-11.csv"))

  (-main "Assets:Checking" (io/resource "2020-11.csv") "/tmp/olar.ldg")

  )
