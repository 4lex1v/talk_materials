#lang racket

(define factf
  (λ (fact)
    (λ (n)
      (if (zero? n)
          1
          (* n
             (fact (- n 1)))))))

(define fibf
  (λ (f)
    (λ (n)
      (if (<= n 2)
          1
          (+ (f (- n 1))
             (f (- n 2)))))))
(define Y
  (λ (f)
    ((λ (x) (f (λ (n) ((x x) n))))
     (λ (x) (f (λ (n) ((x x) n)))))))

((Y fibf) 7)
