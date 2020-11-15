package ru.otus.design;


// now it seems that it's redundant interface, cause nominal can be used instead.
// But in presence of multiple currency there could be method "currency".
public interface Banknote {

    Nominal nominal();
}
