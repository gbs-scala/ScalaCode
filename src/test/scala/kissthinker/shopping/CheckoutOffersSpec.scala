package kissthinker.shopping

import org.specs2.mutable.Specification
import Checkout._

class CheckoutOffersSpec extends Specification {
  val applesDiscount: ShoppingCart => Discount =
    shoppingCart => {
      val apples = shoppingCart.items collect { case a: Apple.type => a }
      math.floor(apples.size / 2) * Apple.price
    }

  val orangesDiscount: ShoppingCart => Discount =
    shoppingCart => {
      val oranges = shoppingCart.items collect { case o: Orange.type => o }
      math.floor(oranges.size / 3) * Orange.price
    }

  val applesAndBananasDiscount: ShoppingCart => Discount =
    shoppingCart => {
      val applesAndBananas = shoppingCart.items collect {
        case item @ (Apple | Banana) => item
      }

      applesAndBananas.map(_.price)
                      .sorted
                      .take(applesAndBananas.size / 2)
                      .sum
    }

  "Apple and banana discounts" should {
    "be given for 2 bananas" in {
      applesAndBananasDiscount(ShoppingCart(Banana, Banana)) mustEqual 0.20
    }

    "be given for 1 banana and 1 apple where the banana is free" in {
      applesAndBananasDiscount(ShoppingCart(Apple, Banana)) mustEqual 0.20
    }

    "be given for 2 bananas and 4 apples taking the cheaper ones for free" in {
      applesAndBananasDiscount(ShoppingCart(Apple, Banana, Apple, Apple, Apple, Banana)) mustEqual 1.00
    }
  }

  "Apple discounts" should {
    "be given for apples" in {
      applesDiscount(ShoppingCart(Apple, Apple)) mustEqual 0.60
    }

    "be given for apples even when there are extra items" in {
      applesDiscount(ShoppingCart(Apple, Orange, Apple)) mustEqual 0.60
    }

    "be given for all apples" in {
      applesDiscount(ShoppingCart(Apple, Apple, Apple, Apple, Apple, Apple, Apple)) mustEqual 1.80
    }

    "not be given when apples don't meet the required offer" in {
      applesDiscount(ShoppingCart(Apple)) mustEqual 0.00
    }

    "not be given for no items" in {
      applesDiscount(ShoppingCart()) mustEqual 0.00
    }
  }

  "Orange discounts" should {
    "be given for oranges" in {
      orangesDiscount(ShoppingCart(Orange, Orange, Orange)) mustEqual 0.25
    }

    "be given for oranges even when there are extra items" in {
      orangesDiscount(ShoppingCart(Orange, Orange, Apple, Orange)) mustEqual 0.25
    }

    "be given for all oranges" in {
      orangesDiscount(ShoppingCart(Orange, Orange, Orange, Orange, Orange, Orange, Orange)) mustEqual 0.50
    }

    "not be given when oranges don't meet the required offer" in {
      orangesDiscount(ShoppingCart(Orange)) mustEqual 0.00
    }

    "not be given for no items" in {
      orangesDiscount(ShoppingCart()) mustEqual 0.00
    }
  }

  "buy 1 apple, get 1 free" should {
    "cost £0.60 for 2 apples on offer" in {
      costOf(ShoppingCart(Apple, Apple), applesDiscount) mustEqual 0.60
    }

    "cost £1.20 for 3 apples on offer" in {
      costOf(ShoppingCart(Apple, Apple, Apple), applesDiscount) mustEqual 1.20
    }

    "cost £1.20 for 3 apples on offer including an oranges offer that is not applicable" in {
      costOf(ShoppingCart(Apple, Apple, Apple), applesDiscount, orangesDiscount) mustEqual 1.20
    }
  }

  "buy 3 oranges, for the price of 2" should {
    "cost £0.50 for 3 oranges on offer" in {
      costOf(ShoppingCart(Orange, Orange, Orange), orangesDiscount) mustEqual 0.50
    }

    "cost £1.25 for 7 oranges on offer" in {
      costOf(ShoppingCart(Orange, Orange, Orange, Orange, Orange, Orange, Orange), orangesDiscount) mustEqual 1.25
    }

    """cost £0.75 for 7 oranges on offer when oranges offer is applied twice - of course this begs the question,
       'is this API a good choice as it would be possible to get a negative price?', though we could change the implementation to use a 'distinct' list of discounts to avoid this issue""" in {
      costOf(ShoppingCart(Orange, Orange, Orange, Orange, Orange, Orange, Orange), orangesDiscount, orangesDiscount) mustEqual 0.75
    }

    "cost £1.25 for 7 oranges on offer including an apples offer that is not applicable" in {
      costOf(ShoppingCart(Orange, Orange, Orange, Orange, Orange, Orange, Orange), orangesDiscount, applesDiscount) mustEqual 1.25
    }
  }

  "buy apples and oranges on offer" should {
    "cost £3.05 for 5 apples and 7 oranges" in {
      costOf(ShoppingCart(Apple, Apple, Apple, Apple, Apple, Orange, Orange, Orange, Orange, Orange, Orange, Orange), applesDiscount, orangesDiscount) mustEqual 3.05
    }
  }

  "buy apples and bananas on offer" should {
    "cost £1.80 for 4 apples and 2 bananas" in {
      costOf(ShoppingCart(Apple, Banana, Apple, Apple, Apple, Banana), applesAndBananasDiscount) mustEqual 1.80
    }

    "cost £2.40 for 5 apples and 2 bananas" in {
      costOf(ShoppingCart(Apple, Banana, Apple, Apple, Apple, Banana, Apple), applesAndBananasDiscount) mustEqual 2.40
    }
  }
}