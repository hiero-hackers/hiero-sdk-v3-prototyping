package org.hiero.sdk.v3.metalang.tck;

import java.util.Map;
import org.hiero.sdk.v3.metalang.MetaLang;
import org.hiero.sdk.v3.metalang.TestSpecs;
import org.hiero.sdk.v3.metalang.model.LinkedModel;

/**
 * A small shop model with a transaction, a query and their result types, a repository of the Mirror Node client, and
 * helpers for bindings files.
 */
final class TckFixtures {

    static final LinkedModel MODEL = LinkedModel.of(new MetaLang().validate(Map.of("f/tx.md", TestSpecs.markdown("""
            namespace consensusnode.transactions

            Transaction {
                @@immutable @@nullable id: string
            }
            """), "f/shop.md", TestSpecs.markdown("""
            namespace shop
            requires {Transaction} from consensusnode.transactions
            requires {Page} from common

            Response<$$R> {
                @@immutable value: $$R
            }

            Detail {
                @@immutable note: string
            }

            OrderReceipt {
                @@immutable orderId: string
                @@immutable @@default([]) serials: list<int64>
                @@immutable count: int64
                @@immutable @@nullable detail: Detail
                @@immutable @@default([]) lines: list<Item>
            }

            Item {
                @@immutable name: string
                @@immutable @@default(false) gift: bool
            }

            @@sealed(SerialGrant, AllGrant)
            abstraction Grant {
            }

            SerialGrant extends Grant {
                @@immutable serials: list<int64>
            }

            AllGrant extends Grant {
                @@immutable all: bool
            }

            OrderTransaction {
                @@immutable customer: string
                @@immutable @@nullable grant: Grant
                @@immutable @@nullable detail: Detail
                @@immutable @@nullable inner: Transaction
                @@immutable @@default([]) tags: set<string>
                @@immutable @@default([]) ids: list<int64>
                @@immutable @@nullable note: string
                @@immutable @@default([]) items: list<Item>
                @@immutable @@default([]) names: list<string>
                @@nullable memo: string
                Response<OrderReceipt> signWithOperatorAndSubmit()
            }

            OrderQuery {
                @@immutable id: string
                @@nullable maxPayment: int64
                Response<Detail> submit()
            }

            Plain {
                @@immutable x: string
            }

            ShopRepository {
                @@async
                Page<Item> items()

                @@async
                Page<Item> items(names: list<string>, @@nullable limit: int32, @@nullable gift: bool)

                @@async
                Detail detail(id: string)
            }
            """), "f/common.md", TestSpecs.markdown("""
            namespace common

            abstraction Page<$$T> {
                @@immutable data: list<$$T>
            }
            """), "f/mirror.md", TestSpecs.markdown("""
            namespace mirrornode
            requires {ShopRepository} from shop

            MirrorNodeClient {
                @@immutable shops: ShopRepository
            }
            """))).model());

    private TckFixtures() {
    }

    /** Wraps bindings into a bindings file. */
    static String file(final String bindings) {
        return "# Shop\n\n```bindings\nrequires {OrderTransaction, OrderQuery, Item, Plain, Detail, SerialGrant, AllGrant, ShopRepository} from shop\n" + bindings
                + "```\n";
    }

    /** Resolves one bindings file. */
    static TckBindings.Bindings resolve(final String bindings) {
        return TckBindings.resolve(Map.of("shop.md", file(bindings)), MODEL);
    }
}
