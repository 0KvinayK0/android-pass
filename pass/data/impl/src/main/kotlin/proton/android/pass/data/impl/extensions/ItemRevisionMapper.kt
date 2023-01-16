package proton.android.pass.data.impl.extensions

import proton.android.pass.data.api.PendingEventItemRevision
import proton.android.pass.data.impl.responses.ItemRevision

fun ItemRevision.toPendingEvent(): PendingEventItemRevision =
    PendingEventItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        rotationId = rotationId,
        content = content,
        userSignature = userSignature,
        itemKeySignature = itemKeySignature,
        state = state,
        signatureEmail = signatureEmail,
        aliasEmail = aliasEmail,
        labels = labels,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUseTime = lastUseTime,
        revisionTime = revisionTime
    )

fun PendingEventItemRevision.toItemRevision(): ItemRevision =
    ItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        rotationId = rotationId,
        content = content,
        userSignature = userSignature,
        itemKeySignature = itemKeySignature,
        state = state,
        signatureEmail = signatureEmail,
        aliasEmail = aliasEmail,
        labels = labels,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUseTime = lastUseTime,
        revisionTime = revisionTime
    )