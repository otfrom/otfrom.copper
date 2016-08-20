(ns otfrom.copper
  (:require [clojure.core.async :as async
             :refer [<! >! <!! >!! timeout chan alt! go]]
            [clojure.java.io :as io]))

;; ideas stolen from here: https://malcolmsparks.com/posts/transducers.html

(defn process-file [in-file out-file xf]
  (let [in    (chan 16)
        out   (chan 100)
        ncpus (+ 1 (.availableProcessors (Runtime/getRuntime)))
        p     (async/pipeline ncpus out xf in)
        fw    (go (with-open [w (io/writer out-file)]
                    (loop []
                      (let [x (<! out)]
                        (when x
                          (.write w x)
                          (recur))))))]
    (with-open [rdr (io/reader in-file)]
      (doseq [line (line-seq rdr)]
        (>!! in line))
      (async/close! in))
    (async/close! fw)))

;; and a process file seq where the file is the level of parallelism?
;; And probably pass in a way to read the files too
;; and possibly a way to limit the number of lines in the output file


(comment

  ;; use that function
  (let [xf (comp (map #(clojure.string/split % #","))
                 (map (fn [[a b c d]] [a d]))
                 (map (fn [[a d]] (str a "," d "\n"))))]
    (process-file "data/test-data.csv" "out.csv" xf))

  )
