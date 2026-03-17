using DrawMap.Domain;

namespace DrawMap.Repositories;

public interface IRouteRepository
{
    Task<string> AddRoute(Route route, CancellationToken cancellationToken);
    Task<Route?> GetRoute(string routeId, CancellationToken cancellationToken);
    Task<List<Route>> GetRoutes(CancellationToken cancellationToken);
    Task<Route?> UpdateRoute(string routeId, Route route, CancellationToken cancellationToken);
    Task<bool> DeleteRoute(string routeId, CancellationToken cancellationToken);
}
