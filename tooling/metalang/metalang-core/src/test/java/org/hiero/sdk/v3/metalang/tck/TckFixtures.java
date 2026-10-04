package org.hiero.sdk.v3.metalang.tck;

import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.model.LinkedModel;

/** A small shop model with a transaction, a query and their result types, and helpers for bindings files. */
final class TckFixtures {

    static final LinkedModel MODEL = LinkedModel.of(new MetaLang().validate(Map.of("f/shop.md", TestSpecs.markdown("""
            namespace shop

            Response<$$R> {
                @@immutable value: $$R
            }

            Detail {
                @@immutable note: string
            }

            OrderReceipt {
                @@immutable orderId: string
                @@immutable count: int64
                @@immutable @@nullable detail: Detail
            }

            Item {
                @@immutable name: string
                @@immutable @@default(false) gift: bool
            }

            OrderTransaction {
                @@immutable customer: string
                @@immutable @@nullable note: string
                @@immutable @@default([]) items: list<Item>
                @@immutable @@default([]) names: list<string>
                @@nullable memo: string
                Response<OrderReceipt> signWithOperatorAndSubmit()
            }

            OrderQuery {
                @@immutable id: string
                Response<Detail> submit()
            }

            Plain {
                @@immutable x: string
            }
            """))).model());

    private TckFixtures() {
    }

    /** Wraps bindings into a bindings file. */
    static String file(final String bindings) {
        return "# Shop\n\n```bindings\nrequires {OrderTransaction, OrderQuery, Item, Plain} from shop\n" + bindings
                + "```\n";
    }

    /** Resolves one bindings file. */
    static TckBindings.Bindings resolve(final String bindings) {
        return TckBindings.resolve(Map.of("shop.md", file(bindings)), MODEL);
    }
}
