package com.example.yuichi.japanesechess;

/**
 * Created by yuichi on 2017/03/01.
 */

public enum PiecesID {
    NOTHING(0),
    OUT_BOARD(99),
    OWN_PAWN(1),
    OWN_BISHOP(2),
    OWN_ROOK(3),
    OWN_LANCE(4),
    OWN_KNIGHT(5),
    OWN_SILVER(6),
    OWN_GOLD(7),
    OWN_KING(8),
    OWN_PROMOTE_PAWN(-1),
    OWN_PROMOTE_BISHOP(-2),
    OWN_PROMOTE_ROOK(-3),
    OWN_PROMOTE_LANCE(-4),
    OWN_PROMOTE_KNIGHT(-5),
    OWN_PROMOTE_SILVER(-6),

    OPP_PAWN(11),
    OPP_BISHOP(12),
    OPP_ROOK(13),
    OPP_LANCE(14),
    OPP_KNIGHT(15),
    OPP_SILVER(16),
    OPP_GOLD(17),
    OPP_KING(18),
    OPP_PROMOTE_PAWN(-11),
    OPP_PROMOTE_BISHOP(-12),
    OPP_PROMOTE_ROOK(-13),
    OPP_PROMOTE_LANCE(-14),
    OPP_PROMOTE_KNIGHT(-15),
    OPP_PROMOTE_SILVER(-16);

    private final int id;

    PiecesID(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
