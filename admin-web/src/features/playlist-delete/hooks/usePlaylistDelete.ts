import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deletePlaylist, playlistKeys } from "@/entities/playlist";

export function usePlaylistDelete() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ ownerId, name }: { ownerId: string; name: string }) =>
      deletePlaylist(ownerId, name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["playlists"] });
      queryClient.invalidateQueries({ queryKey: playlistKeys.stats });
    },
  });
}
