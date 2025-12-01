package de.rogallab.mobile.ui.people.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.Globals
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logComp
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.delay

/**
 * SwipePersonListItemWithoutUndo — Algorithmic Overview
 *
 * PURPOSE
 *  - Displays a row that reacts to horizontal swipe gestures:
 *      • StartToEnd (left → right): triggers navigation to an edit screen
 *      • EndToStart (right → left): triggers a delete animation, then calls `onRemove`
 *  - The gesture is used purely as an *input trigger*. The component itself never
 *    stays in a visually dismissed state. Instead, it runs a controlled exit animation
 *    and delegates the actual data mutation to the ViewModel via `onRemove`.
 *  - This variant is **without Undo**: after the animation finishes, the item is
 *    considered permanently removed from the current UI.
 *
 * STATE
 *  - `isRemoved`: ephemeral UI state that drives `AnimatedVisibility` and the exit animation.
 *      • Scoped by `remember(person.id)` so that when an item with the same id is
 *        re-inserted into the list, its visual state is reset (not removed).
 *  - `SwipeToDismissBoxState`: used only to detect the swipe direction. The state
 *    is immediately snapped back to `Settled` so Compose’s internal dismiss logic
 *    never takes over the visual lifecycle.
 *
 * ALGORITHM
 *  1) User swipes → `state.currentValue` changes.
 *  2) If `StartToEnd`:
 *       • Log and call `onNavigate(person.id)` to navigate to the edit screen.
 *       • Snap the state back to `Settled` (no persistent offset).
 *  3) If `EndToStart`:
 *       • Log and set `isRemoved = true` to start the exit animation.
 *       • Snap the state back to `Settled` (we manage the visuals ourselves).
 *  4) A `LaunchedEffect(isRemoved, person.id)` waits for `animationDuration` and then:
 *       • Calls `onRemove()` so the ViewModel can update its list / repository.
 *  5) When the same `person.id` appears again later, Compose recomposes with a fresh
 *     key scope and `isRemoved` starts as `false`, so the row is visible again.
 *
 * WHY IT WORKS
 *  - **Decoupled gesture & state:** avoiding reliance on SwipeToDismissBox’s internal
 *    dismissal mechanics prevents conflicts and unexpected states.
 *  - **Deterministic removal:** the item disappears only after the exit animation
 *    has finished, giving a smooth, controlled UX without partial states.
 *  - **ViewModel-driven data changes:** the actual removal from the backing list /
 *    repository is handled by `onRemove`, keeping UI and domain logic separated.
 *
 * This pattern is a one-way *Animate-then-Persist* delete:
 * The UI responds first with a smooth animation, then persistence is updated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipePersonListItemWithoutUndo(
   person: Person,
   onNavigate: (String) -> Unit,
   onRemove: () -> Unit,
   content: @Composable () -> Unit
) {
   val tag = "<-SwipePersonLiItem"
   val compositionCount = remember { mutableIntStateOf(1) }
   SideEffect { logComp(tag, "Composition #${compositionCount.intValue++}") }

   // Controls row visibility and exit animation
   var isRemoved: Boolean by remember(person.id) { mutableStateOf(false) }

   val state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(
      positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
   )

   // React to swipe gestures and map them to actions
   LaunchedEffect(state.currentValue) {
      when (state.currentValue) {
         SwipeToDismissBoxValue.StartToEnd -> {
            logDebug(tag, "Swipe to Edit")
            person.id?.let { onNavigate(it) }
            state.snapTo(SwipeToDismissBoxValue.Settled) // Always snap back to Settled
         }
         SwipeToDismissBoxValue.EndToStart -> {
            logDebug(tag, "Swipe to Delete")
            isRemoved = true                                         // Start exit animation
            state.snapTo(SwipeToDismissBoxValue.Settled) // Always snap back to Settled
         }
         SwipeToDismissBoxValue.Settled -> Unit
      }
   }

   // Reset state defensively when the identity of the item changes
   LaunchedEffect(person.id) {
      state.snapTo(SwipeToDismissBoxValue.Settled)
   }

   // After the animation finishes, remove the item permanently (no Undo)
   LaunchedEffect(isRemoved, person.id) {
      if (isRemoved) {
         delay(Globals.animationDuration.toLong())
         onRemove() // Ask the ViewModel to update its list / repository
      }
   }

   AnimatedVisibility(
      visible = !isRemoved,
      exit = shrinkVertically(
         animationSpec = tween(durationMillis = Globals.animationDuration),
         shrinkTowards = Alignment.Top
      ) + fadeOut()
   ) {
      SwipeToDismissBox(
         state = state,
         backgroundContent = { SwipeSetBackground(state) },
         enableDismissFromStartToEnd = true,
         enableDismissFromEndToStart = true,
         modifier = Modifier.padding(vertical = 4.dp)
      ) {
         content()
      }
   }
}