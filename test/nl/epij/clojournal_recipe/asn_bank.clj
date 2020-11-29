(ns nl.epij.clojournal-recipe.asn-bank
  (:require [clojure.test :refer [deftest is]]
            [nl.epij.clojournal-recipe.asn-bank :as asn-bank]
            [com.clojournal.alpha :as clojournal]
            [com.clojournal.alpha.virtual :as virtual]))

(deftest journal-generation
  (is (= (asn-bank/row->tx asn-bank/columns
                           "Assets:Checking"
                           ["28-11-2020"
                            "NL58ASNB0XOXOXOXOX"
                            "NL09INGB00XOXOXOXO"
                            "tegenpartij"
                            ""
                            ""
                            ""
                            "EUR"
                            "13.37"
                            "EUR"
                            "-10.00"
                            "28-11-2020"
                            "28-11-2020"
                            "10101"
                            "OLAR"
                            "7519238"
                            ""
                            "Dit dat zus zo"
                            "42"])
         #::clojournal{:date
                       "2020-11-28"

                       :transaction-id
                       "7519238"

                       :payee
                       "NL09INGB00XOXOXOXO"

                       :memo
                       "Dit dat zus zo"

                       :postings
                       [#::clojournal{:account "Assets:Checking"
                                      :amount  "€ -10,00"
                                      :virtual ::virtual/unbalanced}]})))
