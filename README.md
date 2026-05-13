# GA 2026 — Drugi domaći zadatak — CVRP

## Rezime

Implementirati metaheuristiku koja rešava **problem rutiranja vozila sa kapacitetima** (***Capacitated Vehicle Routing Problem &dash; CVRP***).

Dat je skup kupaca za koje su poznate lokacije i potraživanja robe, skladište i skup vozila sa ograničenim kapacitetima. Cilj je da pronađete rute od skladišta i nazad koje će obići sve kupce i dostaviti im robu, tako da se minimizuje ukupan trošak.

**Izvodljivo rešenje** sastoji se od nekoliko lista sa rutama koje vozila treba da naprave, tako da:

- Svaki kupac mora biti dodeljen tačno jednom vozilu
- Svako vozilo kreće iz skladišta, posećuje jednog ili više kupaca na svojoj ruti i vraća se u skladište
- Ukupno potraživanje robe svih kupaca koji su dodeljeni jednom vozilu ne sme da premaši kapacitet vozila
- Ukupan broj ruta ne sme da premaši broj dostupnih vozila
- Svaka ruta sadrži bar jednog kupca (nema praznih ruta)
- Dozvoljeno je da neka vozila ne napuštaju skladište (samo ih ne treba dodavati u rešenje)

Cilj:
- Minimizovati ukupan trošak
- Trošak = suma proizvoda: (ukupna distanca koju vozilo pređe × koeficijent potrošnje za taj tip vozila)

## Struktura zadatka

### Prvi deo &dash; Klasični CVRP (Domaći zadatak, 20 poena)

Implementirajte algoritam koji minimizuje ukupan trošak u klasi:

```
src/main/java/rs/raf/mtomic/ga2026/cvrp/student/StudentSolver.java
```

### Drugi deo (Kodiranje uživo, obavezan deo + 10 bonus poena)

Na drugom delu radiće se obavezna modifikacija da bi se priznali poeni sa prvog dela, a može se osvojiti i do 10 bonus poena na osnovu postignutih rezultata.

## Bodovanje

| Test                                             | Poeni     | Uslov                                             |
|--------------------------------------------------|-----------|---------------------------------------------------|
| Prvi deo — Izvodljivo rešenje                    | 5         | Validna rešenja na svim testiranim instancama     |
| Prvi deo — U 30% do najboljeg poznatog rezultata | 5         | Minimalni gap na svim instancama <= 30%           |
| Prvi deo — U 15% do najboljeg poznatog rezultata | 5         | Minimalni gap na svim instancama <= 15%           |
| Prvi deo — U 5% do najboljeg poznatog rezultata  | 5         | Minimalni gap na svim instancama <= 5%            |

Prilikom testiranja svaka instanca se automatski testira po tri puta i računa se najbolje od tri dobijena rešenja. Gap za svaki rezultat se računa po formuli

```
double gap = (cost - ref) / ref
```

pri čemu je `ref` referentna vrednost poznatog rezultata u odnosu na koji se računa. Bolji rezultati su bliži referentnoj vrednosti. Ako minimalni gap za svaku instancu nije veći od 5%, dobija se maksimalan broj poena za implementaciju.

Dodatnih 10 poena može se dobiti na odbrani, po implementaciji obavezne modifikacije.

**Ukupno: 20 + 10 bonus = 30 poena max**

## API

### Glavne klase (date, ne menjati!)

**`Instance`** — Problem koji se rešava.
- `getCustomers()` — lista svih kupaca
- `getDepot()` — lokacija skladišta
- `getVehicleCapacity()` — kapacitet vozila
- `getVehicleType()` — tip vozila
- `getDistanceMatrix()` — unapred izračunata matrica distanci (index 0 = skladište, 1..N = kupci)

**`Customer`** — Kupac koji treba da se poseti.
- `getId()`, `getX()`, `getY()`, `getDemand()` — Id, pozicija i potraživanje robe
- `distanceTo(Customer)`, `distanceTo(Depot)` — Euklidska rastojanja do kupca i do skladišta

**`VehicleType`** — Tip vozila
- `getCapacity()` — maksimalni kapacitet vozila (koliko robe može da ponese)
- `getCostPerUnit()` — koeficijent troška po jedinici robe koju nosi
- `getCount()` — koliko vozila tog tipa je dostupno

**`RouteBuilder`** — Pomoćna klasa za kreiranje ruta za rešenje.
- `new RouteBuilder(vehicleType)` — započni novu rutu sa datim tipom vozila (ruta kreće od skladišta)
- `add(customer)` — dodaj kupca
- `canAdd(customer)` — proveri da li se kupac uklapa u preostali kapacitet (na osnovu potrošenog kapaciteta vozila i potraživanja kupca)
- `getRemainingCapacity()` — preostali kapacitet vozila
- `build()` — dovrši i vrati `Route`

**`Solution`** — Vaše rešenje.
- `Solution.of(List<Route> routes)` — napravi rešenje od liste ruta.

### Interfejs Solver

```java
public interface Solver {
    Solution solve(Instance instance, long timeLimitMs);
}
```

Imate `timeLimitMs` milisekundi za izvršavanje algoritma. Pratite vreme na sledeći način:
```java
long deadline = System.currentTimeMillis() + timeLimitMs;
while (System.currentTimeMillis() < deadline) {
    // poboljšaj rešenje
}
```

## Pokretanje u lokalu

```bash
# Pokreni prvi deo na svim instancama
./gradlew runPart1

# Pokreni testove za ocenjivanje lokalno
./gradlew test
```

## Uputstva

- Menjajte **samo** fajlove u paketu `student`
- Možete dodavati klase u paketu `rs.raf.mtomic.ga2026.cvrp.student`
- **Ne menjajte** klase iz drugih paketa, testove, niti instance
- Vremensko ograničenje: 60 sekundi po instanci
- Nisu dozvoljene eksterne biblioteke (samo ono što je već u build.gradle)
